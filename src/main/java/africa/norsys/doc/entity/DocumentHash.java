package africa.norsys.doc.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "document_hashes")
public class DocumentHash {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne
    private Document document;

    @Column(name = "hash_value")
    private String hashValue;
}

