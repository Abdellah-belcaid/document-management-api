package africa.norsys.doc.dto;

import africa.norsys.doc.entity.User;
import africa.norsys.doc.enumerator.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPermissionDto {
    private User user;
    private Permission permission;
}
