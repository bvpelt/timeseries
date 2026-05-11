package com.bsoft.timeseries.entity;

import com.bsoft.timeseries.authentication.model.RoleBody;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "roles", schema = "public", catalog = "adres")
public class RolesEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "rolename")
    private String rolename;

    @Column(name = "description")
    private String description;

    @Column(name = "hash")
    private Integer hash = -1;

    @ManyToMany(mappedBy = "roles") //, fetch = FetchType.LAZY)
    private Collection<UserEntity> users = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "roles_privileges",
            joinColumns = @JoinColumn(name = "roleid", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "privilegeid", referencedColumnName = "id"))
    private Collection<PrivilegeEntity> privileges = new ArrayList<>();

    public RolesEntity(RoleBody roleBody) {
        this.description = roleBody.getDescription();
        this.rolename = roleBody.getRolename();
        this.hash = this.hashCode();
    }

    public Integer genHash() {
        this.hash = hashCode();
        return this.hash;
    }

    public void addUser(UserEntity user) {
        users.add(user);
    }

    public void setPrivileges(Collection<PrivilegeEntity> privileges) {
        privileges.forEach(privilege -> {
            privilege.addRole(this);
            this.privileges.add(privilege);
        });
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RolesEntity rolesDTO = (RolesEntity) o;
        return Objects.equals(rolename, rolesDTO.rolename) && Objects.equals(description, rolesDTO.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rolename, description);
    }

    @Override
    public String toString() {
        return "RoleEntity{" +
                "id=" + id +
                ", rolename='" + rolename + '\'' +
                ", description='" + description + '\'' +
                ", hash=" + hash +
                ", users=" + users +
                ", privileges=" + privileges +
                '}';
    }
}