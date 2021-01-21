package Http;

public enum ResponseCode {

    OK(200, "passed"),
    INTERNAL_SERVER_ERROR(500, "failed");

    private final int intValue;
    private final String description;

    ResponseCode(int intValue, String description) {
        this.intValue = intValue;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getIntValue() {
        return intValue;
    }

    public static ResponseCode fromInt(int value) {
        for (ResponseCode code : values()) {
            if (code.getIntValue() == value) {
                return code;
            }
        }
        throw new IllegalArgumentException("No such response code");
    }
}
