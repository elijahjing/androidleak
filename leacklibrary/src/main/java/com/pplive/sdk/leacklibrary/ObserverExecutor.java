package com.pplive.sdk.leacklibrary;

/**
 * A {@link WatchExecutor} is in charge of executing a {@link Retryable} in the future, and retry
 * later if needed.
 */
public interface ObserverExecutor {
  ObserverExecutor NONE = new ObserverExecutor() {
    @Override public void execute(Retryable retryable) {
    }
  };

  void execute(Retryable retryable);
}
