package com.sin.sb1101.dto;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = "password")
public class Sign implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message= "사용자 이름은 필숩니다.")
    @Size(min = 2, max = 15, message= "사용자 이름은 최소 2자,최대 15자여야합니다")
    private String username;

    @NotBlank(message = "이메일 주소는 필수입니다.")
    @Email(message="유요한 이메일 주소를 입력하세요.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6, max=100, message = "비밀번호는 최소 6자, 최대 100자이여야합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String confirmPassword;

    @Builder.Default
    private int level =1;
    private String department;
    @Builder.Default
    private String role ="User";

//    public boolean isPasswordMatching(){
//        return password !=null && password.equals(confirmPassword);
//    }
}
