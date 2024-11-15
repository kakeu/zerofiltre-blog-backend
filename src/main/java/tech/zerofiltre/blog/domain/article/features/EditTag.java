package tech.zerofiltre.blog.domain.article.features;

import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.error.ResourceAlreadyExistException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;

public class EditTag {

    private final TagProvider tagProvider;

    public EditTag(TagProvider tagProvider) {
        this.tagProvider = tagProvider;
    }

    public Tag create(Tag tag) throws ResourceAlreadyExistException {
        if (tagProvider.tagOfName(tag.getName()).isPresent())
            throw new ResourceAlreadyExistException("A tag with this name already exist.", tag.getName());
        return tagProvider.save(tag);
    }

    public Tag update(Tag tag) throws ResourceNotFoundException {
        if (tagProvider.tagOfId(tag.getId()).isEmpty()) {
            throw new ResourceNotFoundException("We couldn't find a tag of id: " + tag.getId() + " to save", String.valueOf(tag.getId()));
        }
        return tagProvider.save(tag);
    }
}
