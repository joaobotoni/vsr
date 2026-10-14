package com.botoni.vsr.ratelimit;

record Bucket(double tokens, double moment, boolean allowed) {

    private static final double COST = 1;

    static Bucket full(Limit limit, double now) {
        return new Bucket(limit.capacity(), now, false);
    }

    Bucket refill(Limit limit, double now) {
        return advance(limit, periods(limit, now));
    }

    Bucket consume() {
        if (exhausted()) {
            return denied();
        }
        return granted();
    }

    Bucket refund(Limit limit) {
        return new Bucket(refunded(limit), moment, allowed);
    }

    double remaining() {
        return Math.floor(tokens);
    }

    double reset(Limit limit, double now) {
        return limit.interval() - elapsed(now);
    }

    double elapsed(double now) {
        return now - moment;
    }

    private Bucket advance(Limit limit, double periods) {
        return owed(periods) ? next(limit, periods) : this;
    }

    private Bucket next(Limit limit, double periods) {
        return new Bucket(level(limit, periods), later(limit, periods), false);
    }

    private double periods(Limit limit, double now) {
        return Math.floor(elapsed(now) / limit.interval());
    }

    private double level(Limit limit, double periods) {
        return capped(limit, tokens + periods * limit.rate());
    }

    private double later(Limit limit, double periods) {
        return moment + periods * limit.interval();
    }

    private Bucket denied() {
        return new Bucket(tokens, moment, false);
    }

    private Bucket granted() {
        return new Bucket(spent(), moment, true);
    }

    private double spent() {
        return tokens - COST;
    }

    private double refunded(Limit limit) {
        return capped(limit, tokens + COST);
    }

    private static double capped(Limit limit, double tokens) {
        return Math.min(limit.capacity(), tokens);
    }

    private boolean exhausted() {
        return tokens < COST;
    }

    private static boolean owed(double periods) {
        return periods > 0;
    }
}
