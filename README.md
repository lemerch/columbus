```
\ \ .       .             \                  O’er the glad waters of the dark
. \ \       |\            / \_               blue sea,
\ . .       | \            \                 Our thoughts as boundless, and
. \         |  \            \                our souls as free,
 .          |   \           /                Far as the breeze can bear, the
            |    \          \                billows foam,
            |     \                          Survey our empire, and behold
            |      \                         our home.
            |       \       ____O                             «The Corsair». L. Byron
            |        \     .' ./
            |   _.,-~"\  .',/~'
            &lt;-~"   _.,-~" ~ |
^"~-,._.,-~"^"~-,._\       /,._.,-~"^"~-,._.,-~"^"~-,._
~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._
^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._
~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^ COLUMBUS - the discoverer of convenience mapping
```

## About
This library will help you create universal repositories based on NamedParameterJdbcTemplate, generate RowMapper for your models, and link your models to dto or dto to dto.

...dto :)

## Limitations

Your models/dto should contain default constructors and setters with geters, i.e. be JavaBean

## Build

```xml
<repositories>
    <repository>
       <id>jitpack.io</id>
       <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
          <groupId>com.github.lemerch</groupId>
          <artifactId>columbus</artifactId>
          <version>2-6.0.13</version>
    </dependency>
</dependencies>
```

**Warrning**
This library uses spring-jdbc as a dependency. Therefore, for the convenience of users, the version template is arranged as follows: [columbusVersion - spring-dataVersion], for example: 1-6.0.12

## License

The Spring Framework is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).

## Example

### JdbcMapper

```java
import com.github.lemerch.columbus.JdbcMapper;

public abstract class AbstractDAOImpl<MODEL> implements AbstractDAO<MODEL> {

    @Autowired
    protected NamedParameterJdbcTemplate jdbcTemplate;

    protected JdbcMapper.forModel<MODEL> modelMapper;
    protected String table;

    public AbstractDAOImpl(String table, JdbcMapper.forModel<MODEL> modelMapper) {
        this.modelMapper = modelMapper;
        this.table = table;
    }

    @Override
    public List<MODEL> getAll() {
        return jdbcTemplate.query("select * from " + table, modelMapper.get());
    }

    @Override
    public List<MODEL> getAllByColumn(String column, Object value) {
        return jdbcTemplate.query("select * from " + table
                        + " where " + column + " = :value",
                Map.of("value", value), modelMapper.get());
    }

    @Override
    public MODEL getFirstByColumn(String column, Object value) {
        return jdbcTemplate.queryForObject("select * from " + table
                        + " where " + column + " = :value limit 1",
                Map.of("value", value), modelMapper.get());
    }

    @Override
    public <DTO> void create(JdbcMapper.forDTO<DTO> dtoMapper, DTO dto) {
        jdbcTemplate.update("insert into " + table +
                " (" + dtoMapper.columns + ") " +
                "values (" + dtoMapper.values + ")", dtoMapper.getParams(dto));
    }
    
    // OR

    @Override
    public <DTO> void createOnContract(DTO dto) {
        
        JdbcMapper.forContractDTO<DTO> contract = new JdbcMapper.forContractDTO(this.modelMapper, dto);
        
        jdbcTemplate.update("insert into " + table +
                " (" + contract.columns + ") " +
                "values (" + contract.values + ")", contract.params);
    }

}
```

```java
@Repository
public class TestDAOImpl extends AbstractDAOImpl<Test> implements TestDAO {

    private static final RowMapper<Test> rowMapper =
            JdbcMapper.generateRowMapper( Test.class,
                    "id", "id",
                    "name", "sname",
                    "message", "smessage");

    public TestDAOImpl() {
        super("test", rowMapper);
    }
}
```

```java
@Data
public class TestDTO {

    private String name;
    private String message;

    public final static JdbcMapper.forDTO<TestDTO> mapper =
            new JdbcMapper.forDTO<>(TestDTO.class,
                    "name", "sname",
                    "message", "smessage"
            );

}
```

### FieldMapper

```java
@Service
public class TestServiceImpl implements TestService {

    private FieldMapper fieldMapper = new FieldMapper();
    
    @Autowired
    private TestDAO testDAO;

    @Autowired
    private TestMapper testMapper;

    @Override
    public TestDTO getTestByName(String name) {
        return fieldMapper.map(
                testDAO.getFirstByColumn("sname", name), TestDTO.class
        );
    }
    @Override
    public void createTest(TestDTO dto) {
        testDAO.create(dto.mapper, dto);
    }
    
    // OR 

    @Override
    public void createOnContractTest(TestDTO dto) {
        testDAO.createOnContract(dto);
    }
}
```

In addition, you can learn more about FieldMapper in tests [here](src/test/java/com/github/lemerch/columbus/fieldmapper)


```
Мой кров стал — небо голубое,
Корабль стал — родина моя:
Я с ним тогда не расставался
...
                  «Моряк» Лермонтов
```
