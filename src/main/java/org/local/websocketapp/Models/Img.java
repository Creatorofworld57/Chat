package org.local.websocketapp.Models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="images")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Img {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String name;
    private String originalFileName;
    Long size;
    private String ContentType;
    private boolean isPreview;
    @Lob
    private byte[] bytes;


    @OneToOne(cascade = CascadeType.REFRESH,fetch=FetchType.EAGER)
    private UserC user;




}
