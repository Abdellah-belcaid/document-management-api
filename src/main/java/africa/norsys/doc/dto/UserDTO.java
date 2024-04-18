package africa.norsys.doc.dto;


import africa.norsys.doc.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private UUID id;
    private String name;
    private String email;
    private String username;
    private LocalDateTime createTime;
    //private String Image_Path;
    private Role role;
    private String token;
    private String enabled;

}