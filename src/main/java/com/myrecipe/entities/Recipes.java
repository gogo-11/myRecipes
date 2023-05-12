package com.myrecipe.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *Представлява рецепта на даден потребител
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recipes")
public class Recipes implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="recipe_id")
    private Integer id;

    @Column(unique = true)
    private String recipeName;

    @Column(length = 510)
    private String products;

    private Integer portions;

    @Column(name = "cooking_time")
    private Integer cookingTime;

    @Column(name = "cooking_steps", length = 6000)
    private String cookingSteps;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Categories category;

    /**
     * Указва дали рецептата е тайна
     */
    private Boolean isPrivate;

    /**
     * Създава връзка много към един (много рецепти - един потребител)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    @JsonBackReference
    private Users user;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(nullable = true)
    @JsonManagedReference
    private List<Comments> comments;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image")
    private byte[] image;
}