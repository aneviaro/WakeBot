package aneviaro;

enum Errors {
    NotValidTimeFormat("Not a valid time format, please try *22:22*"), FeatureIsNotSupported(
            "Sorry, we don't support this feature right now");

    private final String error;

    Errors(String msg) {
        this.error = msg;
    }

    public String getMessage() {
        return this.error;
    }
}
