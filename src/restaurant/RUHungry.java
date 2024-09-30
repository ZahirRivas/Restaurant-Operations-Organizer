package restaurant;

/** 
 * 
 * Assignment Creator: Mary Buist
 * Assignment Creator: Kushi Sharma
*/

public class RUHungry {
    
    /*
     * Instance variables
     */

    // Menu: two parallel arrays. The index in one corresponds to the same index in the other.
    private   String[] categoryVar; // array where containing the name of menu categories (e.g. Appetizer, Dessert).
    private MenuNode[] menuVar;     // array of lists of MenuNodes where each index is a category.
    
    // Stock: hashtable using chaining to resolve collisions.
    private StockNode[] stockVar;  // array of linked lists of StockNodes (use hashfunction to organize Nodes: id % stockVarSize)
    private int stockVarSize;

    // Transactions: orders, donations, restock transactions are recorded 
    private TransactionNode transactionVar; // refers to the first front node in linked list

    // Queue keeps track of parties that left the restaurant
    private Queue<Party> leftQueueVar;  

    // Tables Information - parallel arrays
    // If tableSeats[i] has 3 seats then parties with at most 3 people can sit at tables[i]
    private Party[] tables;      // Parties currently occupying the tables
    private   int[] tableSeats;  // The number of seats at each table

    /*
     * Default constructor
     */
    public RUHungry () {
        categoryVar    = null;
        menuVar        = null;
        stockVar       = null;
        stockVarSize   = 0;
        transactionVar = null;
        leftQueueVar   = null;
        tableSeats     = null;
        tables         = null;
    }

    /*
     * Getter and Setter methods
     */
    public MenuNode[] getMenu() { return menuVar; }
    public String[] getCategoryArray() { return categoryVar;}
    public StockNode[] getStockVar() { return stockVar; } 
    public TransactionNode getFrontTransactionNode() { return transactionVar; } 
    public TransactionNode resetFrontNode() {return transactionVar = null;} // method to reset the transactions for a new day
    public Queue<Party> getLeftQueueVar() { return leftQueueVar; } 
    public Party[] getTables() { return tables; }
    public int[] getTableSeats() { return tableSeats; }

    /*
     * Menu methods
     */

    /**
     * 
     * This method populates the two parallel arrays menuVar and categoryVar.
     * @param inputFile - use menu.in file which contains all the dishes
     */

    public void menu(String inputFile) {

        StdIn.setFile(inputFile); // opens the inputFile to be read
        int numOfCate = StdIn.readInt();
        // StdIn.readLine();
        categoryVar = new String[numOfCate];
        menuVar = new MenuNode[numOfCate];


        for (int i = 0; i < numOfCate; i++){
            categoryVar[i] = StdIn.readString();
            // size of the linked list in menuVar
            int numOfDishes = StdIn.readInt();
            // StdIn.readLine();

            for (int k = 0; k < numOfDishes;k++){
                StdIn.readLine();
                String nameOfDish = StdIn.readLine();

                int numOfIngredientIDs = StdIn.readInt();

                int [] stockID = new int [numOfIngredientIDs];
                for (int j = 0; j < numOfIngredientIDs; j++){
                    stockID[j] = StdIn.readInt();
                }

                Dish dish = new Dish(categoryVar[i],nameOfDish,stockID);
                MenuNode menuNode = new MenuNode(dish,null);

                if (menuVar[i] == null){
                    menuVar[i] = menuNode;
                } else {
                    menuNode.setNextMenuNode(menuVar[i]);
                    menuVar[i] = menuNode;
                }
            }  
        }
    }

    /*
     * Find and return the MenuNode that contains the dish with dishName in the menuVar.
     * 
     * @param dishName - the name of the dish
     * @return the dish object corresponding to searched dish, null if dishName is not found.
     */

    public MenuNode findDish ( String dishName ) {

        MenuNode menuNode = null;

        // Search all categories since we don't know which category dishName is at
        for ( int category = 0; category < menuVar.length; category++ ) {

            MenuNode ptr = menuVar[category]; // set ptr at the front (first menuNode)
            
            while ( ptr != null ) { // while loop that searches the LL of the category to find the itemOrdered
                if ( ptr.getDish().getName().equalsIgnoreCase(dishName) ) {
                    return ptr;
                } else{
                    ptr = ptr.getNextMenuNode();
                }
            }
        }
        return menuNode;
    }

    /*
     * Find integer that corresponds to the index in menuVar and categoryVar arrays that has that category
     *
     * @param category - the category name
     * @return index of category in categoryVar
     */

    public int findCategoryIndex ( String category ) {
        int index = 0;
        for ( int i = 0; i < categoryVar.length; i++ ){
            if ( category.equalsIgnoreCase(categoryVar[i]) ) {
                index = i;
                break;
            }
        }
        return index;
    }

    /*
     * Stockroom methods
     */

    /**
     * 
     * @param newNode - StockNode that needs to be inserted into StockVar
     */

    public void addStockNode ( StockNode newNode ) {
        int ingredientID = newNode.getIngredient().getID();
        int hashFunction = ingredientID % stockVarSize;

        if (stockVar[hashFunction] == null){
            stockVar[hashFunction] = newNode;
        } else {
            newNode.setNextStockNode(this.stockVar[hashFunction]);
            stockVar[hashFunction] = newNode;
        }
    }

    /*
     * This method finds an ingredient from StockVar (given the ingredientID)
     * @param ingredientID - the ID of the ingredient
     * @return the StockNode corresponding to the ingredientID, null otherwise
     */
   
    public StockNode findStockNode (int ingredientID) {

        int hashFunction = ingredientID % stockVarSize;
        StockNode ptr = stockVar[hashFunction];

        while (ptr != null){
            if (ptr.getIngredient().getID() == ingredientID){
                return ptr;
            }
        ptr = ptr.getNextStockNode();
        }

        return null; // update the return value
    }

    /*
     * This method is to find an ingredient from StockVar (given the ingredient name).
     * 
     * @param ingredientName - the name of the ingredient
     * @return the specific ingredient StockNode, null otherwise
     */

    public StockNode findStockNode (String ingredientName) {
        
        StockNode stockNode = null;
        
        for ( int index = 0; index < stockVar.length; index ++ ){
            
            StockNode ptr = stockVar[index];
            
            while ( ptr != null ){
                if ( ptr.getIngredient().getName().equalsIgnoreCase(ingredientName) ){
                    return ptr;
                } else {  
                    ptr = ptr.getNextStockNode();
                }
            }
        }
        return stockNode;
    }

    /**
     * This method updates the stock amount of an ingredient.
     * 
     * @param ingredientName - the name of the ingredient
     * @param ingredientID - the id of the ingredient
     * @param stockAmountToAdd - the amount to add to the current stock amount
     */
    
    public void updateStock (String ingredientName, int ingredientID, int stockAmountToAdd) {

        // this means we have to use the ingredientID to search and update StockLevel
        if (ingredientName == null){
            int hashFunction = ingredientID % stockVarSize;
            StockNode ptr = stockVar[hashFunction];

            while (ptr != null){
                if (ptr.getIngredient().getID() == ingredientID){
                    ptr.getIngredient().setStockLevel(ptr.getIngredient().getStockLevel() + stockAmountToAdd);
                }
            ptr = ptr.getNextStockNode();
            }
        // this means we have to use ingredientName to search and update stockLevel
        } else if (ingredientID == -1){
            StockNode nameStockNode = findStockNode(ingredientName);
            if ((nameStockNode.getIngredient().getName()).equals(ingredientName)){
                nameStockNode.getIngredient().setStockLevel(nameStockNode.getIngredient().getStockLevel() + stockAmountToAdd);
            }
        }

    }

    /**
     * This method goes over menuVar to update the price and profit of each dish,
     * using the stockVar hashtable to lookup for ingredient's costs.
     * 
     * @return void
     */

    public void updatePriceAndProfit() {
        for (int i = 0; i < menuVar.length;i++){
            MenuNode ptr = menuVar[i];

            // for each dish in menuVar[i]
            while(ptr != null){

            // compute the dish cost by adding up the cost of each ingredient 
            double dishCost = 0.0;
            int [] stockID = ptr.getDish().getStockID();
            
            for (int j = 0; j < stockID.length;j++ ){
                dishCost += findStockNode(stockID[j]).getIngredient().getCost();
            }

                // updating price of dish
                ptr.getDish().setPriceOfDish(dishCost * 1.2);
                // updating the profit each dish makes the restaurant
                ptr.getDish().setProfit(ptr.getDish().getPriceOfDish() - dishCost);

            ptr = ptr.getNextMenuNode();
            }            
        }
    }

    /**
     * 
     * This method initializes and populates stockVar which is a hashtable where each index contains a 
     * linked list with StockNodes.
     * 
     * @param inputFile - the input file with the ingredients and all their information (stock.in)
     */

    public void createStockHashTable (String inputFile){
        
        StdIn.setFile(inputFile); // opens inputFile to be read by StdIn
        int sizeOfStockVar = StdIn.readInt();
        this.stockVarSize = sizeOfStockVar;
        stockVar = new StockNode[sizeOfStockVar];
        StdIn.readLine();

        while (StdIn.hasNextLine()){
            int ingredientID = StdIn.readInt();
            StdIn.readChar();
            String ingredientName = StdIn.readLine();
            double costOfIngredient = StdIn.readDouble();
            int stockAmount = StdIn.readInt();

            Ingredient ingredient = new Ingredient(ingredientID, ingredientName, stockAmount, costOfIngredient);
            StockNode stockNode = new StockNode(ingredient, null);
            addStockNode(stockNode);
        }
    }

    /*
     * Transaction methods
     */

    /**
     * This method adds a TransactionNode to the END of the transactions linked list.
     * The front of the list is transactionVar.
     *
     * 
     * @param data - TransactionData node to be added to transactionVar
     */

    public void addTransactionNode ( TransactionData data ) { // method adds new transactionNode to the end of LL
        
        if (transactionVar == null){
            transactionVar = new TransactionNode(data, null);
        } else {
            TransactionNode ptr = transactionVar;
            while (ptr.getNext()!= null){
                ptr = ptr.getNext();
            }
            ptr.setNext(new TransactionNode(data, null));
        }
    }

    /**
     * *************
     * This method checks if there's enough in stock to prepare a dish.
     * 
     * 1. use findDish() method to find the menuNode node for dishName
     * 
     * 2. retrieve the Dish, then traverse ingredient array within the Dish
     * 
     * 3. return boolean based on whether you can sell the dish or not
     * HINT! --> once you determine you can't sell the dish, break and return
     * 
     * @param dishName - String of dish that's being requested
     * @param numberOfDishes - int of how many of that dish is being ordered
     * @return boolean
     */

    public boolean checkDishAvailability (String dishName, int numberOfDishes){

        MenuNode dishToCheck = findDish(dishName);

        int [] dishIngredients = dishToCheck.getDish().getStockID();


        for (int i = 0; i < dishIngredients.length; i++){
            StockNode stockNode = findStockNode(dishIngredients[i]);
            if (stockNode.getIngredient().getStockLevel() < numberOfDishes){
                return false;
            }
        }
        return true; // update the return value
    }

    /**
     * ***************
     * This method simulates a customer ordering a dish. Use the checkDishAvailability() method to check whether the dish can be ordered.
     * If the dish cannot be prepared
     *      - create a TransactionData object of type "order" where the item is the dishName, the amount is the quantity being ordered, and profit is 0 (zero).
     *      - then add the transaction as an UNsuccessful transaction and,
     *      - simulate the customer trying to order other dishes in the same category linked list:
     *          - if the dish that comes right after the dishName can be prepared, great. If not, try the next one and so on.
     *          - you might have to traverse through the entire category searching for a dish that can be prepared. If you reach the end of the list, start from the beginning until you have visited EVERY dish in the category.
     *          - It is possible that no dish in the entire category can be prepared.
     *          - Note: the next dish the customer chooses is always the one that comes right after the one that could not be prepared. 
     * 
     * @param dishName - String of dish that's been ordered
     * @param quantity - int of how many of that dish has been ordered
     */


     // helper method to update the stock of EACH ingredient that the dish contains
    private void updateIngredientStock(String dishName, int quantity){
        int [] stockIngredients = findDish(dishName).getDish().getStockID();
            for (int i = 0; i < stockIngredients.length;i++){
                updateStock(null, stockIngredients[i], - quantity);
            }
    }

    public void order (String dishName, int quantity){

        MenuNode initialDish = findDish(dishName);

        // if the dish could be prepared with initial parameters
        if (checkDishAvailability(dishName, quantity)){
            TransactionData transactionData = new TransactionData("order", dishName, quantity, findDish(dishName).getDish().getProfit() * quantity, true);
            addTransactionNode(transactionData);
        // updating the stock of each ingredient of the dish
            updateIngredientStock(dishName,quantity);
            return;

        } else {
            // this portion checks whether or not the following dishes in the MenuNode are availible for order (iterates through the linked list until one is available)
            MenuNode followingPtr = findDish(dishName);

            while((followingPtr != null) && (!checkDishAvailability(followingPtr.getDish().getName(), quantity))){
                TransactionData failedTransaction = new TransactionData("order", followingPtr.getDish().getName(), quantity, 0,false);
                addTransactionNode(failedTransaction);
                followingPtr = followingPtr.getNextMenuNode();
            }
            // there would be only two reasons why it came out the while loop; 1. it found an available dish; 2. it still hasnt and it must loop back from the beginning to check for an available dish

            // if any dish can be prepared from the next dish (after the initial dish) to any dish up to the end of the linked list, this will add the transaction
            if (followingPtr != null && checkDishAvailability(followingPtr.getDish().getName(), quantity)){
                TransactionData newTransactionData = new TransactionData("order", followingPtr.getDish().getName(), quantity, followingPtr.getDish().getProfit() * quantity , true);
                addTransactionNode(newTransactionData);
                updateIngredientStock(followingPtr.getDish().getName(),quantity);
                return;

            } else {

                MenuNode frontPtr = menuVar[findCategoryIndex(initialDish.getDish().getCategory())];
                // this will start from the beginning and iterate through the linked list until the intialDish is met to try and look for dish that could be prepared
                while (frontPtr != null && frontPtr != initialDish && !checkDishAvailability(frontPtr.getDish().getName(), quantity)){
                    TransactionData failedTransaction = new TransactionData("order", frontPtr.getDish().getName(), quantity, 0,false);
                    addTransactionNode(failedTransaction);
                    frontPtr = frontPtr.getNextMenuNode();
                }

                if (checkDishAvailability(frontPtr.getDish().getName(), quantity)){
                    TransactionData frontTransaction = new TransactionData("order", frontPtr.getDish().getName(), quantity, frontPtr.getDish().getProfit() * quantity, true);
                    addTransactionNode(frontTransaction);
                    updateIngredientStock(frontPtr.getDish().getName(),quantity);
                    return;
                }
            }
        }
    }

    /**
     * This method returns the total profit for the day
     *
     * The profit is computed by traversing the transaction linked list (transactionVar) 
     * adding up all the profits for the day
     * 
     * @return profit - double value of the total profit for the day
     */

    public double profit () {

	TransactionNode ptr = transactionVar;
    double profit = 0.0;

    while (ptr != null){
        profit += ptr.getData().getProfit();
        ptr = ptr.getNext();
    }
        return profit; // update the return value
    }

    /**
     * This method simulates donation requests, successful or not.
     * 
     * 1. check whether the profit is > 50 and whether there's enough ingredients in stock.
     * 
     * 2. add transaction to transactionVar
     * 
     * @param ingredientName - String of ingredient that's been requested
     * @param quantity - int of how many of that ingredient has been ordered
     * @return void
     */

    public void donation (String ingredientName, int quantity){
        StockNode stockNode = findStockNode(ingredientName);
        if (profit() > 50.0 && (stockNode.getIngredient().getStockLevel() >= quantity)){
            TransactionData transaction = new TransactionData("donation", ingredientName, quantity, 0, true);
            addTransactionNode(transaction);
            updateStock(ingredientName, -1, - quantity);
        } else {
            TransactionData unsuccessfulTransaction = new TransactionData("donation", ingredientName, quantity, 0, false);
            addTransactionNode(unsuccessfulTransaction);
        }

    }

    /**
     * This method simulates restock orders
     * 
     * 1. check whether the profit is sufficient to pay for the total cost of ingredient
     *      a) (how much each ingredient costs) * (quantity)
     *      b) if there is enough profit, adjust stock and profit accordingly
     * 
     * 2. add transaction to transactionVar
     * 
     * @param ingredientName - ingredient that's been requested
     * @param quantity - how many of that ingredient needs to be ordered
     */

    public void restock (String ingredientName, int quantity){
        StockNode stockNode = findStockNode(ingredientName);
        double costOfRestock = stockNode.getIngredient().getCost() * quantity;
        if (profit() >= costOfRestock){
            TransactionData transaction = new TransactionData("restock", ingredientName, quantity, -costOfRestock, true);
            addTransactionNode(transaction);
            updateStock(ingredientName, -1, quantity);
        } else {
            TransactionData unsuccessfulTransaction = new TransactionData("restock", ingredientName, quantity, 0, false);
            addTransactionNode(unsuccessfulTransaction);
        }
    }

   /*
    * Seat guests/customers methods
    */

    /**
     * Method to populate tables (which is a 1D integer array) based upon input file
     * 
     * The input file is formatted as follows:
     * - an integer t contains the number of tables
     * - t lines containing number of rows * seats per row for each table
     * 
     * @param inputFile - tables1.in (contains all the tables in the RUHungry restaurant)
     * @return void (aka nothing)
     */

    public void createTables ( String inputFile ) { 

        StdIn.setFile(inputFile);
	
        int numberOfTables = StdIn.readInt();
        tableSeats = new int[numberOfTables];
        tables     = new Party[numberOfTables];

        for ( int t = 0; t < numberOfTables; t++ ) {
            tableSeats[t] = StdIn.readInt() * StdIn.readInt();
        }
    }


    /**
     * Prints all states of the restaurant.
     * 
     */
    public void printRestaurant() {
        // 1. Print out menu
        StdOut.println("Menu:");
        if (categoryVar != null) {
            for (int i=0; i < categoryVar.length; i++) {
                StdOut.print(categoryVar[i] + ":");
                StdOut.println();

                MenuNode ptr = menuVar[i];
                while (ptr != null) {
                    StdOut.print(ptr.getDish().getName() + "  Price: $" +
                    ((Math.round(ptr.getDish().getPriceOfDish() * 100.0)) / 100.0) + " Profit: $" + ((Math.round(ptr.getDish().getProfit() * 100.0)) / 100.0));
                    StdOut.println();

                    ptr = ptr.getNextMenuNode();
                }
                StdOut.println();
            }
        }
        else {
            StdOut.println("Empty - categoryVar is null.");
        }
        // 2. Print out stock
        StdOut.println("Stock:");
        if (stockVar != null) {
            for (int i=0; i < 10; i++) {
                StdOut.println("Index " + i);
                StockNode ptr = stockVar[i];
                while (ptr != null) {
                    StdOut.print(ptr.getIngredient().getName() + "  ID: " + ptr.getIngredient().getID() + " Price: " +
                    ((Math.round(ptr.getIngredient().getCost() *100.0)) / 100.0) + " Stock Level: " + ptr.getIngredient().getStockLevel());
                    StdOut.println();
    
                    ptr = ptr.getNextStockNode();
                }
    
                StdOut.println();
            }
        }
        else {
            StdOut.println("Empty - stockVar is null.");
        }
        // 3. Print out transactions
        StdOut.println("Transactions:");
        if (transactionVar != null) {
            TransactionNode ptr = transactionVar;
            int successes = 0;
            int failures = 0;
            while (ptr != null) {
                String type = ptr.getData().getType();
                String item = ptr.getData().getItem();
                int amount = ptr.getData().getAmount();
                double profit = ptr.getData().getProfit();
                boolean success = ptr.getData().getSuccess();
                if (success == true){
                    successes += 1;
                }
                else if (success == false){
                    failures += 1;
                }

                StdOut.println("Type: " + type + ", Name: " + item + ", Amount: " + amount + ", Profit: $" + ((Math.round(profit * 100.0)) / 100.0) + ", Was it a Success? " + success);
                
                ptr = ptr.getNext();
            }
            StdOut.println("Total number of successful transactions: " + successes);
            StdOut.println("Total number of unsuccessful transactions: " + failures);
            StdOut.println("Total profit remaining: $" + ((Math.round(profit() * 100.0)) / 100.0));
        }
        else {
            StdOut.println("Empty - transactionVar is null.");
        }
        // 4. Print out tables
        StdOut.println("Tables and Parties:");
        restaurant.Queue<Party> leftQueue = leftQueueVar;
        if (leftQueueVar != null) {
            StdOut.println(("Parties in order of leaving:"));
            int counter = 0;
            while (!leftQueue.isEmpty()) {
                Party removed = leftQueue.dequeue();
                counter += 1;
                StdOut.println(counter + ": " + removed.getName());
            }
        }
        else {
            StdOut.println("Empty -- leftQueueVar is empty");
        }
    }
}
