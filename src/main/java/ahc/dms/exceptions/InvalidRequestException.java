package ahc.dms.exceptions;

public class InvalidRequestException extends RuntimeException {
    String resourceField;
    String resourceName;
    String invalidMsg;

    public InvalidRequestException(String resourceField, String resourceName, String invalidMsg) {
        super(String.format("%s : %s : %s", resourceField, resourceName, invalidMsg));
        this.resourceField = resourceField;
        this.resourceName = resourceName;
        this.invalidMsg = invalidMsg;
    }

}
