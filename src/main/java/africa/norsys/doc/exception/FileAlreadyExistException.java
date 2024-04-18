package africa.norsys.doc.exception;

public class FileAlreadyExistException extends RuntimeException {
    public FileAlreadyExistException(String message) {
        super(message);
    }
}
