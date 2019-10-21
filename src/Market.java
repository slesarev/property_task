import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Market {
    private static Logger log = Logger.getLogger(Market.class.getName());

    private static volatile Market instance;

    private Market(String fileName) throws Exception {
        doRefresh(fileName);
    }

    public static Market getInstance(String fileName) throws Exception {
        if (instance == null) {
            synchronized (Market.class) {
                if (instance == null)
                    instance = new Market(fileName);
            }
        }
        return instance;
    }

    @Property(propertyName = "market.name")
    private String name;

    @Property(propertyName = "market.numberOfOrange", def = "5")
    private Integer numberOfOrange;

    @Property(propertyName = "market.numberOfApple")
    private Integer numberOfApple;

    @Property(propertyName = "market.amountOfMilk", def = "12.")
    private Double amountOfMilk;

    @Property(propertyName = "market.address")
    private Address address;

    public Object checkType(String type) {
        if (type.matches("^\\{.+\\}$")) {
            Gson gson = new Gson();
            Address address = gson.fromJson(type, Address.class);
            return (address);
        } else if (type.matches("\\d+$")) {
            return (new Integer(type));
        } else if (type.matches("\\d+\\.\\d*$")) {
            return (new Double(type));
        } else return type;
    }

    public synchronized void doRefresh(String fileName) throws Exception {
        Class cl = Class.forName("Market");
        Field[] field = cl.getDeclaredFields();
        try {
            FileReader fr = new FileReader(fileName);
            Scanner scan = new Scanner(fr);
            Set<Field> usedFields = new HashSet<>();
            while (scan.hasNextLine()) {
                String nextLine = scan.nextLine();
                if (nextLine.matches("\\S+\\s+=.+$")) {
                    String[] typeValue = nextLine.split("=");
                    typeValue[0] = typeValue[0].replaceAll(" ", "");
                    typeValue[1] = Pattern.compile("^\\s+").matcher(typeValue[1]).replaceFirst("");
                    for (Field fl : field) {
                        if (fl.isAnnotationPresent(Property.class)) {
                            if (fl.getAnnotation(Property.class).propertyName().equals(typeValue[0])) {
                                Object value = checkType(typeValue[1]);
                                if (value.getClass().equals(fl.getType())) {
                                    fl.set(this, value);
                                    usedFields.add(fl);
                                    break;
                                } else {
                                    log.info("Data type error: " + nextLine + " must be " + fl.getType().toString());
                                }
                            }
                        }
                    }
                } else {
                    log.info("This line isn't an expression: " + nextLine);
                }
            }
            for (Field fl : field) {
                if (fl.isAnnotationPresent(Property.class)) {
                    if (!usedFields.contains(fl))
                        if (!fl.getAnnotation(Property.class).def().equals("")) {
                            Object value = checkType(fl.getAnnotation(Property.class).def());
                            if (fl.getType().equals(value.getClass()))
                                fl.set(this, value);
                        } else fl.set(this, null);
                }
            }
            fr.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Exception: ", e);
            throw new RuntimeException(e);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setNumberOfOrange(Integer numberOfOrange) {
        this.numberOfOrange = numberOfOrange;
    }

    public void setNumberOfApple(Integer numberOfApple) {
        this.numberOfApple = numberOfApple;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public Integer getNumberOfOrange() {
        return numberOfOrange;
    }

    public Integer getNumberOfApple() {
        return numberOfApple;
    }

    public Double getAmountOfMilk() {
        return amountOfMilk;
    }

    public void setAmountOfMilk(Double amountOfMilk) {
        this.amountOfMilk = amountOfMilk;
    }
}
