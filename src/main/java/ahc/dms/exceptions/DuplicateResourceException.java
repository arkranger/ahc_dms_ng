package ahc.dms.exceptions;

public class DuplicateResourceException extends RuntimeException {
    String resourceField;
    String resourceName;

    public DuplicateResourceException(String resourceField, String resourceName) {
        super(String.format("%s : %s already exists", resourceField, resourceName));
        this.resourceField = resourceField;
        this.resourceName = resourceName;
    }

}
