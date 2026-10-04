package com.botoni.vsr.infra.email;

import java.nio.file.Path;

public interface Sender {

    void send(Details details);

    void send(Details details, Path... attachments);
}