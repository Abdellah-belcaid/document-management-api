package africa.norsys.doc.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;



import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "creation_date")
    @CreationTimestamp
    private LocalDate creationDate;


    @Column(name = "storage_location")
    private String storageLocation;

    @ElementCollection
    @CollectionTable(name = "document_metadata", joinColumns = @JoinColumn(name = "document_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> metadata;

}
