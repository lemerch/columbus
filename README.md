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
...

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
          <version>6.0.12</version>
    </dependency>
</dependencies>
```

**Warrning**
this library uses spring-data, so I copy the columbus version from spring-data for ease of connection to your project

Example: yet, columbus use spring-data 6.0.12, so columbus version will be 6.0.12

## License

The Spring Framework is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).

## Example

### JdbcTemplate

```java
public abstract class AbstractDAOImpl<MODEL, DTO> implements AbstractDAO<MODEL, DTO> {
    
    @Autowired
    protected NamedParameterJdbcTemplate jdbcTemplate;
    
    protected JdbcMapper.forModel<MODEL> modelMapper;
    protected JdbcMapper.forDTO<DTO> dtoMapper;
    protected String table;
    
    public AbstractDAOImpl(
            String table, JdbcMapper.forModel<> modelMapper, JdbcMapper.forDTO<> dtoMapper
    ) {
        this.modelMapper = modelMapper;
        this.dtoMapper = dtoMapper;
        this.table = table;
    }

    @Override
    public List<MODEL> getAll(Long id) {
        return jdbcTemplate.query("select * from " + table, modelMapper.getRowMapper());
    }
    @Override
    public List<MODEL> getAllByColumn(String column, Object value) {
        return jdbcTemplate.query("select * from " + table
                        + " where " + column + " = :value",
                Map.of("value", value) , modelMapper.getRowMapper());
    }
    @Override
    public MODEL getFirstByColumn(String column, Object value) {
        return jdbcTemplate.queryForObject("select * from " + table
                        + " where " + column + " = :value limit 1",
                Map.of("value", value), modelMapper.getRowMapper());
    }
    @Override
    public void create(DTO dto) {
        jdbcTemplate.update("insert into " + table +
                " (" + this.dtoMapper.getColumns() + ") " +
                "values (" + dtoMapper.getValues() + ")", dtoMapper.getParams(dto));
    }

}
```

```java
@Repository
public class TestDAOImpl extends AbstractDAOImpl<Test, TestDTO> implements TestDAO {
    public TestDAOImpl() {
        super("test_table",
                new JdbcMapper.forModel<>(Test.class,
                        "id", "id",
                        "name", "sname",
                        "message", "smessage"
                ),
                new JdbcMapper.forDTO<>(TestDTO.class,
                        "name", "sname",
                        "message", "smessage"
                )
        );
    }
}
```

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
        testDAO.create(dto);
    }
}
```

