import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/* ---------- STOCK CLASS ---------- */
class Stock implements Serializable {
    private String symbol;
    private String companyName;
    private double price;
    private double previousPrice;

    public Stock(String symbol, String companyName, double price) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.price = price;
        this.previousPrice = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        previousPrice = this.price;
        this.price = price;
    }

    public double getChange() {
        return price - previousPrice;
    }

    public double getChangePercent() {
        return previousPrice == 0 ? 0 :
                (getChange() / previousPrice) * 100;
    }

    public void display() {
        System.out.printf(
            "%-10s %-20s %10.2f %10.2f %10.2f%%%n",
            symbol, companyName, price,
            getChange(), getChangePercent()
        );
    }
}


/* ---------- TRANSACTION CLASS ---------- */
class Transaction implements Serializable {
    private String type;
    private String symbol;
    private int quantity;
    private double price;
    private String dateTime;

    public Transaction(String type, String symbol,
                       int quantity, double price) {
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;

        DateTimeFormatter f =
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        dateTime = LocalDateTime.now().format(f);
    }

    public void display() {
        System.out.printf(
            "%-8s %-10s %-10d %-12.2f %s%n",
            type, symbol, quantity, price, dateTime
        );
    }
}


/* ---------- PORTFOLIO CLASS ---------- */
class Portfolio implements Serializable {

    private HashMap<String, Integer> holdings =
            new HashMap<>();

    private ArrayList<Transaction> transactions =
            new ArrayList<>();

    public void buyStock(Stock stock, int quantity) {

        if (quantity <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }

        String symbol = stock.getSymbol();

        holdings.put(
            symbol,
            holdings.getOrDefault(symbol, 0) + quantity
        );

        transactions.add(
            new Transaction(
                "BUY",
                symbol,
                quantity,
                stock.getPrice()
            )
        );

        System.out.println(
            "Bought " + quantity + " shares of " + symbol
        );
    }

    public boolean sellStock(Stock stock, int quantity) {

        String symbol = stock.getSymbol();

        if (quantity <= 0) {
            System.out.println("Invalid quantity.");
            return false;
        }

        int available =
                holdings.getOrDefault(symbol, 0);

        if (available < quantity) {
            System.out.println("Insufficient shares.");
            return false;
        }

        int remaining = available - quantity;

        if (remaining == 0)
            holdings.remove(symbol);
        else
            holdings.put(symbol, remaining);

        transactions.add(
            new Transaction(
                "SELL",
                symbol,
                quantity,
                stock.getPrice()
            )
        );

        System.out.println(
            "Sold " + quantity + " shares of " + symbol
        );

        return true;
    }

    public void displayPortfolio(
            HashMap<String, Stock> market) {

        System.out.println("\n========== PORTFOLIO ==========");

        if (holdings.isEmpty()) {
            System.out.println("Portfolio is empty.");
            return;
        }

        double total = 0;

        System.out.printf(
            "%-10s %-10s %-15s%n",
            "Symbol", "Quantity", "Value"
        );

        for (Map.Entry<String, Integer> e :
                holdings.entrySet()) {

            Stock stock = market.get(e.getKey());

            if (stock != null) {
                double value =
                        e.getValue() * stock.getPrice();

                total += value;

                System.out.printf(
                    "%-10s %-10d %-15.2f%n",
                    e.getKey(),
                    e.getValue(),
                    value
                );
            }
        }

        System.out.println("-------------------------------");
        System.out.printf(
            "Total Portfolio Value: %.2f%n",
            total
        );
    }

    public double getPortfolioValue(
            HashMap<String, Stock> market) {

        double total = 0;

        for (Map.Entry<String, Integer> e :
                holdings.entrySet()) {

            Stock stock = market.get(e.getKey());

            if (stock != null)
                total +=
                    e.getValue() * stock.getPrice();
        }

        return total;
    }

    public void displayPerformance(
            HashMap<String, Stock> market) {

        System.out.println(
            "\n====== PORTFOLIO PERFORMANCE ======"
        );

        double value = getPortfolioValue(market);

        System.out.printf(
            "Current Portfolio Value: %.2f%n",
            value
        );

        System.out.println(
            "Performance changes with market prices."
        );
    }

    public void displayTransactions() {

        System.out.println(
            "\n====== TRANSACTION HISTORY ======"
        );

        if (transactions.isEmpty()) {
            System.out.println("No transactions.");
            return;
        }

        System.out.printf(
            "%-8s %-10s %-10s %-12s %s%n",
            "Type", "Symbol", "Quantity",
            "Price", "Date & Time"
        );

        for (Transaction t : transactions)
            t.display();
    }
}


/* ---------- USER CLASS ---------- */
class User implements Serializable {

    private String userId;
    private String name;
    private double balance;
    private Portfolio portfolio;

    public User(String userId, String name,
                double balance) {

        this.userId = userId;
        this.name = name;
        this.balance = balance;
        portfolio = new Portfolio();
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void buyStock(Stock stock, int quantity) {

        if (quantity <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }

        double cost =
                stock.getPrice() * quantity;

        if (balance < cost) {
            System.out.println(
                "Insufficient account balance."
            );
            return;
        }

        balance -= cost;

        portfolio.buyStock(stock, quantity);

        System.out.printf(
            "Remaining Balance: %.2f%n",
            balance
        );
    }

    public void sellStock(Stock stock, int quantity) {

        boolean success =
                portfolio.sellStock(stock, quantity);

        if (success) {

            balance +=
                stock.getPrice() * quantity;

            System.out.printf(
                "Updated Balance: %.2f%n",
                balance
            );
        }
    }

    public void displayDetails() {

        System.out.println("\n========== USER ==========");

        System.out.println("User ID : " + userId);
        System.out.println("Name    : " + name);

        System.out.printf(
            "Balance : %.2f%n",
            balance
        );
    }
}


/* ---------- MAIN CLASS ---------- */
public class StockTradingPlatform {

    static Scanner sc = new Scanner(System.in);

    static HashMap<String, Stock> market =
            new HashMap<>();

    static User user;

    static final String FILE =
            "portfolio.dat";


    /* MARKET DATA */
    static void initializeMarket() {

        market.put(
            "AAPL",
            new Stock("AAPL", "Apple Inc.", 180)
        );

        market.put(
            "MSFT",
            new Stock("MSFT", "Microsoft", 420)
        );

        market.put(
            "GOOG",
            new Stock("GOOG", "Google", 175)
        );

        market.put(
            "AMZN",
            new Stock("AMZN", "Amazon", 185)
        );

        market.put(
            "TSLA",
            new Stock("TSLA", "Tesla", 250)
        );
    }


    /* DISPLAY MARKET */
    static void displayMarket() {

        System.out.println(
            "\n============= MARKET DATA ============="
        );

        System.out.printf(
            "%-10s %-20s %10s %10s %10s%n",
            "Symbol", "Company",
            "Price", "Change", "Change%"
        );

        for (Stock stock : market.values())
            stock.display();
    }


    /* UPDATE MARKET PRICES */
    static void updateMarketPrices() {

        Random random = new Random();

        for (Stock stock : market.values()) {

            double change =
                    random.nextDouble() * 10 - 5;

            double newPrice =
                    stock.getPrice() + change;

            if (newPrice > 0)
                stock.setPrice(newPrice);
        }

        System.out.println(
            "Market prices updated."
        );
    }


    /* BUY */
    static void buyStock() {

        displayMarket();

        System.out.print(
            "\nEnter stock symbol: "
        );

        String symbol =
                sc.next().toUpperCase();

        Stock stock = market.get(symbol);

        if (stock == null) {
            System.out.println("Stock not found.");
            return;
        }

        System.out.print("Enter quantity: ");

        int quantity = sc.nextInt();

        user.buyStock(stock, quantity);
    }


    /* SELL */
    static void sellStock() {

        displayMarket();

        System.out.print(
            "\nEnter stock symbol: "
        );

        String symbol =
                sc.next().toUpperCase();

        Stock stock = market.get(symbol);

        if (stock == null) {
            System.out.println("Stock not found.");
            return;
        }

        System.out.print("Enter quantity: ");

        int quantity = sc.nextInt();

        user.sellStock(stock, quantity);
    }


    /* SAVE FILE */
    static void saveData() {

        try {

            ObjectOutputStream out =
                    new ObjectOutputStream(
                        new FileOutputStream(FILE)
                    );

            out.writeObject(user);

            out.close();

            System.out.println(
                "Portfolio saved successfully."
            );

        } catch (IOException e) {

            System.out.println(
                "Error saving data."
            );
        }
    }


    /* LOAD FILE */
    static void loadData() {

        try {

            ObjectInputStream in =
                    new ObjectInputStream(
                        new FileInputStream(FILE)
                    );

            user = (User) in.readObject();

            in.close();

            System.out.println(
                "Previous portfolio loaded."
            );

        } catch (Exception e) {

            user = new User(
                "U101",
                "Student",
                100000
            );
        }
    }


    /* MAIN */
    public static void main(String[] args) {

        initializeMarket();

        loadData();

        int choice;

        do {

            System.out.println(
                "\n================================"
            );

            System.out.println(
                "     STOCK TRADING PLATFORM"
            );

            System.out.println(
                "================================"
            );

            System.out.println(
                "1. Display Market Data"
            );

            System.out.println(
                "2. Buy Stock"
            );

            System.out.println(
                "3. Sell Stock"
            );

            System.out.println(
                "4. View Portfolio"
            );

            System.out.println(
                "5. View Performance"
            );

            System.out.println(
                "6. Transaction History"
            );

            System.out.println(
                "7. User Details"
            );

            System.out.println(
                "8. Update Market Prices"
            );

            System.out.println(
                "9. Save Portfolio"
            );

            System.out.println(
                "10. Exit"
            );

            System.out.print(
                "Enter choice: "
            );

            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    displayMarket();
                    break;

                case 2:
                    buyStock();
                    break;

                case 3:
                    sellStock();
                    break;

                case 4:
                    user.getPortfolio()
                        .displayPortfolio(market);
                    break;

                case 5:
                    user.getPortfolio()
                        .displayPerformance(market);
                    break;

                case 6:
                    user.getPortfolio()
                        .displayTransactions();
                    break;

                case 7:
                    user.displayDetails();
                    break;

                case 8:
                    updateMarketPrices();
                    break;

                case 9:
                    saveData();
                    break;

                case 10:
                    saveData();
                    System.out.println(
                        "Thank you!"
                    );
                    break;

                default:
                    System.out.println(
                        "Invalid choice."
                    );
            }

        } while (choice != 10);

        sc.close();
    }
}