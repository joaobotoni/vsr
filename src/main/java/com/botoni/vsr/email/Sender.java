package com.botoni.vsr.email;

import java.nio.file.Path;

public interface Sender {

    void send(Details details);

    void send(Details details, Path... attachments);
}
