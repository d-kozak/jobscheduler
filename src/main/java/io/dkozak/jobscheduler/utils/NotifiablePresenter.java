package io.dkozak.jobscheduler.utils;

public interface NotifiablePresenter {
    void showInfoMessage(String message);

    void showErrorMessage(String message);
}
