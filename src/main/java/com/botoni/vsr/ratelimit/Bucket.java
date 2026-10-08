package com.botoni.vsr.ratelimit;

record Bucket(double tokens, double lastRefill, boolean allowed) {
}
