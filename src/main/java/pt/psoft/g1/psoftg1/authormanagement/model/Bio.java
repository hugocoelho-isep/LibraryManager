package pt.psoft.g1.psoftg1.authormanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pt.psoft.g1.psoftg1.shared.model.StringUtils;

@Embeddable
public class Bio {
    @Column(nullable = false, length = 4096)
    @NotNull
    @Size(min = 1, max = 4096)
    String Bio;

    public Bio(String bio) {
        setBio(bio);
    }

    protected Bio() {
    }

    private void setBio(String bio) {
        this.Bio = StringUtils.sanitizeHtml(bio);
    }
}

