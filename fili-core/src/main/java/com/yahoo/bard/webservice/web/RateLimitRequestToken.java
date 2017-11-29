// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.web;

import java.io.Closeable;

/**
 * Resource representing an outstanding request.
 */
public abstract class RateLimitRequestToken implements Closeable {
    /**
     * Check if the token is bound.
     *
     * @return true if bound or false if rejected
     */
    public abstract boolean isBound();

    /**
     * Bind the counters to the token.
     *
     * @return true if the token was able to be bound or is already bounf, or false if rejected.
     */
    public abstract boolean bind();

    /**
     * Release the token's counters.
     */
    public abstract void unBind();
}