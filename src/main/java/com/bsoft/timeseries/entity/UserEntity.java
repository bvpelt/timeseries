package com.bsoft.timeseries.entity;

import com.bsoft.timeseries.authentication.model.User;
import com.bsoft.timeseries.authentication.model.UserBody;
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
@Table(name = "users", schema = "public", catalog = "adres")
public class UserEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    @ManyToOne
    @JoinColumn(name = "apikey_id")
    ApiKeyEntity apiKey;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;
    @Column(name = "email")
    private String email;
    @Column(name = "phone")
    private String phone;
    @Column(name = "account_non_expired")
    private Boolean account_non_expired = true;
    @Column(name = "account_non_locked")
    private Boolean account_non_locked = true;
    @Column(name = "credentials_non_expired")
    private Boolean credentials_non_expired = true;
    @Column(name = "enabled")
    private Boolean enabled = true;
    @Column(name = "hash")
    private Integer hash = -1;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) // owning site
    @JoinTable(
            name = "users_roles",
            joinColumns = {
                    @JoinColumn(name = "userid", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "roleid", referencedColumnName = "id")
            }
    )
    private Collection<RolesEntity> roles = new ArrayList<>();

    public UserEntity(String username, String password, String email, String phone) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.hash = hashCode();
    }

    public UserEntity(final UserBody userbody) {
        this.setUsername(userbody.getUsername());
        this.setPassword(userbody.getPassword());
        this.setEmail(userbody.getEmail());
        this.setPhone(userbody.getPhone());
        this.setAccount_non_expired(userbody.getAccountNonExpired());
        this.setAccount_non_locked(userbody.getAccountNonLocked());
        this.setCredentials_non_expired(userbody.getCredentialsNonExpired());
        this.setEnabled(userbody.getEnabled());
        this.setRoles(new ArrayList<>());
        this.hash = hashCode();
    }

    public UserEntity(final User user) {
        this.setUsername(user.getUsername());
        this.setPassword(user.getPassword());
        this.setEmail(user.getEmail());
        this.setPassword(user.getPhone());
        this.setAccount_non_expired(user.getAccountNonExpired());
        this.setAccount_non_locked(user.getAccountNonLocked());
        this.setCredentials_non_expired(user.getCredentialsNonExpired());
        this.setEnabled(user.getEnabled());
        this.setRoles(new ArrayList<>());
    }

    public Integer genHash() {
        this.hash = hashCode();
        return this.hash;
    }

    public void addRole(RolesEntity role) {
        roles.add(role);
    }

    public void setRoles(Collection<RolesEntity> roles) {
        roles.forEach(role -> {
            role.addUser(this);
            this.roles.add(role);
        });
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity userDTO = (UserEntity) o;
        return Objects.equals(username, userDTO.username) && Objects.equals(password, userDTO.password) && Objects.equals(email, userDTO.email) && Objects.equals(phone, userDTO.phone) && Objects.equals(account_non_expired, userDTO.account_non_expired) && Objects.equals(account_non_locked, userDTO.account_non_locked) && Objects.equals(credentials_non_expired, userDTO.credentials_non_expired) && Objects.equals(enabled, userDTO.enabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, email, phone, account_non_expired, account_non_locked, credentials_non_expired, enabled);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", accountNonExpired=" + account_non_expired +
                ", accountNonLocked=" + account_non_locked +
                ", credentialsNonExpired=" + credentials_non_expired +
                ", enabled=" + enabled +
                // ", roles=" + roles +
                ", hash=" + hash +
                '}';
    }
}