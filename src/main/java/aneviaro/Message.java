package aneviaro;

enum Messages {

    BestTimeToWakeUp("The best time to wake up is:\n"), BestTimeToGoToSleep(
            "The best time to go to sleep is:\n"), Greetings(
            "Greeting, please type in the time in the next format: I.e. *22:15*"), ClarificationQuestion(
            "Is it wake up time or go to sleep time?");

    private final String message;

    Messages(String msg) {
        this.message = msg;
    }

    public String getMessage() {
        return this.message;
    }
}
