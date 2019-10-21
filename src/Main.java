public class Main {

    public static void main(String[] args) throws Exception {

        Market ma = Market.getInstance("src\\test1.properties");
        ma.doRefresh("src\\test2.properties");

    }

}
