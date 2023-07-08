package bootmapper;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

// ---------------------------------------------------------------------------
// Example request body for POST /parent/create
// ---------------------------------------------------------------------------
//
// {
//   "children": [
//     {
//       "name": "Kid"
//     }
//   ]
// }
//
// ---------------------------------------------------------------------------

@SpringBootApplication
public class BootMapperApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootMapperApplication.class, args);
    }
}

// ---------------------------------------------------------------------------

@Entity
@Data
class Parent {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<Child> children;

    public void setChildren(Set<Child> children) {
        if (this.children != null) {
            this.children.forEach(child -> child.setParent(null));
        }
        if (children != null) {
            children.forEach(child -> child.setParent(this));
        }
        this.children = children;
    }
}

@Entity
@Data
class Child {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Parent parent;

    @Column(length = 1024)
    private String name;
}

// ---------------------------------------------------------------------------

@Data
class ParentDTO {

    private UUID id;

    private Set<ChildDTO> children;
}

@Data
class ChildDTO {

    private UUID id;

    private UUID parentId;

    private String name;
}

// ---------------------------------------------------------------------------

@Mapper(componentModel = "spring", uses = ChildMapper.class)
interface ParentMapper {

    ParentDTO toDTO(Parent entity);

    Parent toEntity(ParentDTO dto);
}

@Mapper(componentModel = "spring")
interface ChildMapper {

    @Mapping(source = "parent.id", target = "parentId")
    ChildDTO toDTO(Child entity);

    @Mapping(source = "parentId", target = "parent.id")
    Child toEntity(ChildDTO dto);
}

// ---------------------------------------------------------------------------

interface ParentRepository extends JpaRepository<Parent, UUID> {
}

// ---------------------------------------------------------------------------

@Service
@Transactional
@RequiredArgsConstructor
class ParentService {

    private final ParentMapper parentMapper;

    private final ParentRepository parentRepository;

    public ParentDTO save(ParentDTO dto) {
        Parent parent = parentMapper.toEntity(dto);
        parent = parentRepository.save(parent);
        return parentMapper.toDTO(parent);
    }
}

// ---------------------------------------------------------------------------

@RestController
@RequestMapping("parent")
@RequiredArgsConstructor
class ParentResource {

    private final ParentService parentService;

    @PostMapping("create")
    public ParentDTO create(@RequestBody ParentDTO dto) {
        return parentService.save(dto);
    }
}

// ---------------------------------------------------------------------------
