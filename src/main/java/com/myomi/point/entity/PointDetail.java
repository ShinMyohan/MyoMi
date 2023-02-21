package com.myomi.point.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor @ToString
@Entity
@Table(name="point_detail")
@DynamicInsert
@DynamicUpdate
public class PointDetail{
   @EmbeddedId
   private PointDetailEmbedded pointEmbedded;
   
    @Column(name="sort" ,updatable = false)
    @NotNull
	private int sort;
	
	@Column(name="amount" ,updatable = false)
	@NotNull
	private int amount;
	
	@ManyToOne
	@JoinColumn(name="user_id",insertable = false ,updatable = false)
	private Point point;
}