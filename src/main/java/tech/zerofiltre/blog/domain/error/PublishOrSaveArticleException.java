package tech.zerofiltre.blog.domain.error;

public class PublishOrSaveArticleException extends ZerofiltreException {

    private final long itemId;

    public PublishOrSaveArticleException(String message, long itemId) {
        super(message);
        this.itemId = itemId;
    }

    public long getItemId() {
        return itemId;
    }
}
