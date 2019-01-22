import java.util.Random;

@SuppressWarnings("unchecked")

class cuckooHash<T>{

    //Define constants for the table
    private final int GROWTH_FACTOR = 2;
    private final int MIN_TABLE_SIZE = 10;
    private float MIN_LOAD = (float) 0.2;
    private float MAX_LOAD = (float) 0.4;

    //General hash table variables
    private int size = 0;
    private Random random = new Random();

    //Table 1 specific variables
    private Object[][] table1;
    private int occupied1 = 0;
    private int table1Hash = random.nextInt((2^32));

    //Table 2 specific variables
    private Object[][] table2;
    private int occupied2 = 0;
    private int table2Hash = random.nextInt((2^32));

    //Default constructor
    public cuckooHash(){

        this(10, (float) 0.2, (float) 0.4);

    }

    //Construct with specified min and max load factors
    public cuckooHash(float min, float max){

        this(10, min, max);

    }

    //Construct with specified size
    public cuckooHash(int size, float min, float max){

        this.size = size;
        this.table1 = new Object[size][4];
        this.table2 = new Object[size][4];
        this.MIN_LOAD = min;
        this.MAX_LOAD = max;

    }

    //Return the load factor of the hash table
    public float getLoad(){
        
        float occupied = occupied1 + occupied2;
        float size = 2*this.size;

        return occupied/size;
    };

    //Insert allowing resize bypass
    public Boolean insert(T o, Boolean bypass){

        int hash1 = hashFunc(o, table1Hash);
        int hash2 = hashFunc(o, table2Hash);

        if(table1[hash1][0] != null) if(table1[hash1][0].equals(o)) return false;
        if(table2[hash2][0] != null) if(table2[hash2][0].equals(o)) return false;

        if(table1[hash1][0] == null){

            table1[hash1][0] = o;
            table1[hash1][1] = hash1;
            table1[hash1][2] = hash2;
            table1[hash1][3] = 1;

            occupied1++;

            if(!bypass) resize();
            return true;
        }
        else if(table2[hash2][0] == null){

            table2[hash2][0] = o;
            table2[hash2][1] = hash1;
            table2[hash2][2] = hash2;
            table2[hash2][3] = 2;

            occupied2++;

            if(!bypass) resize();
            return true;
        }
        //Handle cuckoo kicking
        else{

            if(kick(table1, hash1, 0)) return insert(o, false);
            else{
                reHash(o, true);
                return true;
            }

        }

    }

    //Return true if the object is added, false if the element already exists or an error occurs
    public Boolean insert(T o){
        return insert(o, false);
    }

    //Delete the given key from the table
    public Boolean delete(T o){

        int hash1 = hashFunc(o, table1Hash);
        int hash2 = hashFunc(o, table2Hash);

        if(table1[hash1][0] != null) if(table1[hash1][0].equals(o)){
            table1[hash1][0] = null;
            occupied1--;
            return true;
        }

        if(table2[hash2][0] != null) if(table2[hash2][0].equals(o)){
            table2[hash2][0] = null;
            occupied2--;
            return true;
        }

        return false;
    }

    //Return the index of given key
    public int get(T o){
        
        int hash1 = hashFunc(o, table1Hash);
        int hash2 = hashFunc(o, table2Hash);

        if(table1[hash1][0] != null) if(table1[hash1][0].equals(o)) return hash1;
        if(table2[hash2][0] != null) if(table2[hash2][0].equals(o)) return size+hash2;

        return -1;

    }

    //Return the index of given key
    public int getNoOffset(T o){
        
        int hash1 = hashFunc(o, table1Hash);
        int hash2 = hashFunc(o, table2Hash);

        if(table1[hash1][0] != null) if(table1[hash1][0].equals(o)) return hash1;
        if(table2[hash2][0] != null) if(table2[hash2][0].equals(o)) return hash2;

        return -1;

    }

    //Return hash table as an object array
    protected Object[] getHashObj(){
        
        Object[] newHashObj = new Object[7];

        newHashObj[0] = table1;
        newHashObj[1] = occupied1;
        newHashObj[2] = table1Hash;
        newHashObj[3] = table2;
        newHashObj[4] = occupied2;
        newHashObj[5] = table2Hash;
        newHashObj[6] = size;

        return newHashObj;

    }

    //Rehash table without adding object
    public void reHash(){
        reHash(null, false);
    }

    //Rehash the table
    private void reHash(T toAdd, Boolean add){

        //Generate new table
        cuckooHash<T> newHash = new cuckooHash<>(size, MIN_LOAD, MAX_LOAD);
        Boolean rehashing = true;

        //Copy over all elements and ensure no collisions
        while(rehashing){

            rehashing = false;

            for(Object[] o : table1){
                if(o[0] != null) rehashing |= !newHash.insert((T) o[0], true);
            }

            for(Object[] o : table2){
                if(o[0] != null) rehashing |= !newHash.insert((T) o[0], true);
            }

            if(add) rehashing |= !newHash.insert(toAdd, true);
            rehashing = !rehashing;

        }

        //Update table
        copy(newHash);

    }

    //Calculate the hashes for the object using a given seed value
    public int hashFunc(T o, int seed){

        int hash = o.hashCode();
        Random rand = new Random();
        rand.setSeed(hash*seed);
        hash = rand.nextInt();

        return Math.abs(hash%size);
    }

    //Resize the table when load factor exceeds the given bounds
    private void resize(){

        float load = getLoad();

        if(load < MIN_LOAD && size > MIN_TABLE_SIZE){

            cuckooHash<T> newHash = new cuckooHash<>(size/GROWTH_FACTOR, MIN_LOAD, MAX_LOAD);
            Boolean success = true;

            for(Object[] o : table1){
                if(o[0] != null) success &= newHash.insert((T) o[0], true);
            }

            for(Object[] o : table2){
                if(o[0] != null) success &= newHash.insert((T) o[0], true);
            }

            if(!success) newHash.reHash();

            copy(newHash);

        }

        else if(load > MAX_LOAD){

            cuckooHash<T> newHash = new cuckooHash<>(size*GROWTH_FACTOR, MIN_LOAD, MAX_LOAD);
            Boolean success = true;

            for(Object[] o : table1){
                if(o[0] != null) success &= newHash.insert((T) o[0], true);
            }

            for(Object[] o : table2){
                if(o[0] != null) success &= newHash.insert((T)o[0], true);
            }

            if(!success) newHash.reHash();

            copy(newHash);
            
        }

    }

    //Copy values from one cuckooHash to another
    public void copy(cuckooHash h){

        Object[] toCopy = h.getHashObj();

        this.table1 = (Object[][]) toCopy[0];
        this.occupied1 = (int) toCopy[1];
        this.table1Hash = (int) toCopy[2];
        this.table2 = (Object[][]) toCopy[3];
        this.occupied2 = (int) toCopy[4];
        this.table2Hash = (int) toCopy[5];
        this.size = (int) toCopy[6];

    }

    //Begin recursive kick operation in order to make space in the table
    private Boolean kick(Object[][] table, int index, int currDepth){

        Boolean success = false;

        //Ensure we aren't in a loop
        if(currDepth < Math.sqrt(size)){
            //Determine where we are removing from
            Object[] toMove = table[index];
            int usedIndex = (int) toMove[3];
            int toUse = (int) table[index][1];
            if(usedIndex == 1) toUse = (int) table[index][2];

            if(usedIndex == 1){
                //If we cant move it to its other position, kick
                if(table2[toUse][0] != null){
                    success = kick(table2, get((T) table2[toUse][0])-size, currDepth+1);
                }
                //Otherwise add to alternate position
                if(table2[toUse][0] == null){
                    table2[toUse][0] = toMove[0];
                    table2[toUse][1] = toMove[1];
                    table2[toUse][2] = toMove[2];
                    table2[toUse][3] = toMove[2];
                    table1[index][0] = null;
                    return true;
                }
            }
            else if(usedIndex == 2){
                //If we cant move it to its other position, kick
                if(table1[toUse][0] != null){
                    success = kick(table1, get((T) table1[toUse][0]), currDepth+1);
                }
                //Otherwise add to alternate position
                if(table1[toUse][0] == null){
                    table1[toUse][0] = toMove[0];
                    table1[toUse][1] = toMove[1];
                    table1[toUse][2] = toMove[2];
                    table1[toUse][3] = toMove[2];
                    table2[index][0] = null;
                    return true;
                }
            }
        }

        return success;

    }

    //Print the table
    @Override
    public String toString(){

        String str1 = "[";
        String str2 = "[";

        for(Object[] o : table1){
            if(o[0] != null) str1 += o[0].toString()+" ";
            else str1 += "_ ";
        }

        for(Object[] o : table2){
            if(o[0] != null) str2 += o[0].toString()+" ";
            else str2 += "_ ";
        }

        str1 = str1.substring(0, str1.length()-1) + "]";
        str2 = str2.substring(0, str2.length()-1) + "]";

        return (str1+"\n"+str2);

    }

}