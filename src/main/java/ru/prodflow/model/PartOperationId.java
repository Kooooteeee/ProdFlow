package ru.prodflow.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class PartOperationId implements Serializable {

    @Column(name = "part_id")
    private Long partId;

    @Column(name = "sequence")
    private Integer sequence;
}
