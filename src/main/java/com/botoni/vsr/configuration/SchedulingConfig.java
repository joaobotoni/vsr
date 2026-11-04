package com.botoni.vsr.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Tarefas periódicas da aplicação (limpeza de arquivos órfãos de evidência).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
