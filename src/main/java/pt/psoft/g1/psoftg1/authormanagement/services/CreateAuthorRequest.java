package pt.psoft.g1.psoftg1.authormanagement.services;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

//@Data
//@EqualsAndHashCode(callSuper = true)
//public class CreateAuthorRequest extends UpdateAuthorRequest {
//    public CreateAuthorRequest(final String name, final String bio){
//        super(name, bio);
//    }
//
//}
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A DTO for creating a Author")
public class CreateAuthorRequest {
    private String name;

    @Size(min = 1, max = 4096)
    private String bio;
}
