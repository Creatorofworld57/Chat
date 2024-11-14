package org.local.websocketapp.Services;




import lombok.AllArgsConstructor;

import org.local.websocketapp.Models.AuthRequest;
import org.local.websocketapp.Models.Img;
import org.local.websocketapp.Models.UserC;
import org.local.websocketapp.Repositories.ImageRepository;
import org.local.websocketapp.Repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;



@Service
@AllArgsConstructor
public class ServiceForUser {
    private UserRepository repository;
    private ImageRepository imageRepository;
    private PasswordEncoder passwordEncoder;


    public static Img toImgEntity(MultipartFile file) throws IOException {
        Img img = new Img();
        img.setName(file.getName());
        img.setOriginalFileName(file.getOriginalFilename());
        img.setContentType(file.getContentType());
        img.setSize(file.getSize());
        img.setBytes(file.getBytes());
        return img;

    }


    public String addUser(AuthRequest request, MultipartFile file) throws IOException {
        Img img = new Img();
        UserC user = new UserC();
        user.setName(request.getName());
        user.setPassword(request.getPassword());

        if (file != null && file.getSize() != 0) {
            img = toImgEntity(file);
            img.setPreview(true);
            user.addImgToProduct(img);
        }
        Date date = new Date();
        user.setUpdatedAt(date);
        user.setCreatedAt(date);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        UserC userok = repository.save(user);
        Img img1 = imageRepository.save(img);
        userok.setPreviewImageId(img1.getId());
        repository.save(userok);
        return "good";

    }
    public void Registration (MultipartFile file, AuthRequest request){

    }











}
