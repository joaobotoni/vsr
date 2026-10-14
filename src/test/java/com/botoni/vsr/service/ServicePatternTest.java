package com.botoni.vsr.service;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Padrão dos serviços: o público só lista passos; todo desvio de fluxo (if, try, throw, orElseThrow) fica em auxiliar;
 * a ordem é públicos, passos, auxiliares de desvio de fluxo e booleanos; cada método tem no máximo um nível de indentação;
 * booleanos não usam prefixo is, has ou can e não existem só para negar outro booleano.
 */
@DisplayName("Padrão dos serviços")
class ServicePatternTest {

    private static final Path SERVICES = Path.of("src/main/java/com/botoni/vsr/service");
    private static final Pattern METHOD = Pattern.compile("^    (public|private|protected)\\b[^=;]*?(\\w+)\\s*\\(.*\\)\\s*(throws [^{]*)?\\{\\s*$");
    private static final Pattern FLOW = Pattern.compile("\\bif \\(|\\btry \\{|\\bthrow new|orElseThrow");
    private static final Pattern CALL = Pattern.compile("(\\w+)\\(");
    private static final Pattern BOOLEAN = Pattern.compile("\\bboolean \\w+\\s*\\(");
    private static final Pattern PREFIXED = Pattern.compile("\\bboolean (is|has|can)[A-Z]\\w*\\s*\\(");
    private static final Pattern NEGATION = Pattern.compile("^\\s*return !(\\w+)\\(.*\\);\\s*$");
    private static final Set<String> KEYWORDS = Set.of("for", "if", "while", "return", "new");
    private static final Set<String> TRAILING = Set.of("real");
    private static final int PUBLIC = 0;
    private static final int STEP = 1;
    private static final int BRANCH = 2;
    private static final int PREDICATE = 3;
    private static final String OUT_OF_PATTERN = """
            public class OutOfPattern {

                public String find(String id) {
                    if (id == null) {
                        throw new IllegalArgumentException();
                    }
                    return id;
                }

                private void guard(String id) {
                    if (isEmpty(id)) {
                        throw new IllegalStateException();
                    }
                }

                private String step(String id) {
                    return id.trim();
                }

                private void nested(String[] ids) {
                    for (String id : ids) {
                        if (id.isEmpty()) {
                            throw new IllegalStateException();
                        }
                    }
                }

                private static boolean isEmpty(String id) {
                    return id.isEmpty();
                }

                private static boolean present(String id) {
                    return !isEmpty(id);
                }
            }
            """;

    static Stream<Path> services() throws IOException {
        return Files.list(SERVICES).filter(file -> file.toString().endsWith(".java")).sorted();
    }

    @Controle
    @MethodSource("services")
    @ParameterizedTest(name = "{0}")
    @DisplayName("públicos sem desvio de fluxo, um nível de indentação e desvios de fluxo no fim")
    void serviceFollowsPattern(Path file) throws IOException {
        List<Method> methods = methods(Files.readAllLines(file));
        Set<String> branches = branches(methods);

        assertThat(violations(methods, branches)).as(file.getFileName().toString()).isEmpty();
    }

    @Test
    @Controle
    @DisplayName("o verificador reprova público com desvio de fluxo, passo fora de ordem e indentação aninhada")
    void checkerRejectsViolations() {
        List<Method> methods = methods(OUT_OF_PATTERN.lines().toList());

        assertThat(violations(methods, branches(methods))).containsExactly(
                "find: público com desvio de fluxo",
                "step: fora de ordem",
                "nested: mais de um nível de indentação",
                "isEmpty: booleano com prefixo",
                "present: booleano gêmeo negado");
    }

    private static List<String> violations(List<Method> methods, Set<String> branches) {
        List<String> violations = new ArrayList<>();
        Set<String> predicates = predicates(methods);
        int phase = PUBLIC;
        for (Method method : methods) {
            int kind = method.kind(branches);
            violations.addAll(method.violations(kind, phase, predicates));
            phase = Math.max(phase, kind);
        }
        return violations;
    }

    private static Set<String> predicates(List<Method> methods) {
        Set<String> predicates = new HashSet<>();
        for (Method method : methods) {
            method.collect(predicates);
        }
        return predicates;
    }

    private static Set<String> branches(List<Method> methods) {
        Set<String> branches = new HashSet<>();
        for (Method method : methods) {
            method.mark(branches);
        }
        for (int pass = 0; pass < methods.size(); pass++) {
            for (Method method : methods) {
                method.aggregate(branches);
            }
        }
        return branches;
    }

    private static List<Method> methods(List<String> lines) {
        List<Method> methods = new ArrayList<>();
        Method current = null;
        for (String line : lines) {
            Matcher matcher = METHOD.matcher(line);
            if (!line.contains(" class ") && matcher.matches()) {
                current = new Method(matcher.group(2), matcher.group(1).equals("public"), line, new ArrayList<>());
                methods.add(current);
            } else if (current != null && line.equals("    }")) {
                current = null;
            } else if (current != null) {
                current.body().add(line);
            }
        }
        return methods;
    }

    private record Method(String name, boolean visible, String head, List<String> body) {

        String text() {
            return String.join("\n", body);
        }

        boolean isFlow() {
            return FLOW.matcher(text()).find();
        }

        boolean isPredicate() {
            return BOOLEAN.matcher(head).find();
        }

        void collect(Set<String> predicates) {
            if (isPredicate()) {
                predicates.add(name);
            }
        }

        boolean isTwin(Set<String> predicates) {
            List<String> lines = body.stream().filter(line -> !line.isBlank()).toList();
            Matcher matcher = NEGATION.matcher(lines.size() == 1 ? lines.get(0) : "");
            return isPredicate() && matcher.matches() && predicates.contains(matcher.group(1));
        }

        void mark(Set<String> branches) {
            if (isFlow()) {
                branches.add(name);
            }
        }

        void aggregate(Set<String> branches) {
            List<String> calls = calls();
            if (!visible && !calls.isEmpty() && branches.containsAll(calls)) {
                branches.add(name);
            }
        }

        List<String> calls() {
            List<String> calls = new ArrayList<>();
            Matcher matcher = CALL.matcher(text());
            while (matcher.find()) {
                if (!KEYWORDS.contains(matcher.group(1))) {
                    calls.add(matcher.group(1));
                }
            }
            return calls;
        }

        int kind(Set<String> branches) {
            if (visible) {
                return PUBLIC;
            }
            if (isPredicate()) {
                return PREDICATE;
            }
            return branches.contains(name) ? BRANCH : STEP;
        }

        List<String> violations(int kind, int phase, Set<String> predicates) {
            List<String> violations = new ArrayList<>();
            if (visible && !isPredicate() && isFlow()) {
                violations.add(name + ": público com desvio de fluxo");
            }
            if (depth() > 1) {
                violations.add(name + ": mais de um nível de indentação");
            }
            if (PREFIXED.matcher(head).find()) {
                violations.add(name + ": booleano com prefixo");
            }
            if (isTwin(predicates)) {
                violations.add(name + ": booleano gêmeo negado");
            }
            if (kind < phase && !TRAILING.contains(name)) {
                violations.add(name + ": fora de ordem");
            }
            return violations;
        }

        int depth() {
            int depth = 0;
            String previous = "{";
            for (String line : body) {
                if (line.isBlank()) {
                    continue;
                }
                boolean continuation = !previous.stripTrailing().matches(".*[{};]$");
                previous = line;
                if (!continuation && !line.strip().matches("^[.)+].*")) {
                    depth = Math.max(depth, (line.length() - line.stripLeading().length() - 8) / 4);
                }
            }
            return depth;
        }
    }
}
