
package main.com.rfrench.jvm.java;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import static main.com.rfrench.jvm.java.Main.JSON_FILE_PATH;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
    Program Title: ClassLoader.java
    Author: Ryan French
    Created: 19-Oct-2016
    Version: 1.0
*/

public class ClassLoader
{    
       
    private int NUMBER_OF_METHODS;
                             
    private HashMap bytecode_details_map;              
          
    private JSONArray bytecode_json_array;
    
    private ArrayList<Method> methods;
    
    private MethodArea method_area;
    
    private final String FILE_PATH;
        
    /**
     * JVMFileReader Constructor
     * @param method_area
     * @param FILE_PATH
    */
    public ClassLoader(MethodArea method_area, String FILE_PATH)
    {          
        this.method_area = method_area;
        this.FILE_PATH = FILE_PATH;
        
    }
                                  
    /**
     * Read a javap file. Retrieve relevant data and put into 
     * helper data structures to then be used later in program
     */
    public void readFile()
    {          
        try
        {
            JSONArray bytecode_json = createJSONParser();
            
            createByteCodeDetailsHashMap(bytecode_json);                
            
            parseConstantPoolData();
            
            extractNumberOfMethods();
                                    
            methods = new ArrayList<Method>();
            
            ArrayList<String> method_names_list = findMethodNames();
            
            ArrayList<String> method_access_type_list = findMethodAccess();
            
            ArrayList<Boolean> is_method_instance_list = checkInstanceMethod(method_names_list);
            
            for(int i = 0; i < NUMBER_OF_METHODS; i++)
            {
                ArrayList<String> method_code = parseMethod(i);
                
                String method_name = method_names_list.get(i);
                
                String method_access = method_access_type_list.get(i);
                
                boolean instance_method = is_method_instance_list.get(i);
                
                Method m = new Method(bytecode_details_map, bytecode_json_array, method_code);                               
                
                m.setMethodName(method_name);
                
                m.setMethodAccess(method_access);             
                
                m.setInstanceMethod(instance_method);
                
                methods.add(m);
            }
        }
        
        catch(IOException | ParseException e)
        {
            e.printStackTrace();
        }
    }       

    //DEBUGGING METHOD
    public void readFileTest()
    {                  
        try
        {        
            parseConstantPoolData();
            
            findMethodNames();
            
            
        }
        
        catch(Exception e) 
        {
            e.printStackTrace();
        }
    }   
        
    private JSONArray createJSONParser() throws IOException, ParseException
    {                        
        JSONParser parser = new JSONParser();
            
        Object obj = parser.parse(new InputStreamReader(getClass().getResourceAsStream(JSON_FILE_PATH)));

        JSONObject jsonObject = (JSONObject) obj;
            
        bytecode_json_array = (JSONArray) jsonObject.get("bytecodes");
        
        return bytecode_json_array;
    }
    
    private void createByteCodeDetailsHashMap(JSONArray bytecode_json)
    {
        final int MAX_BYTECODES = 255;
        
        final String ATTRIBUTE = "Name"; //Name of Attribute
        
        bytecode_details_map = new HashMap(MAX_BYTECODES);                      
        
        final int JSON_MAX_SIZE = bytecode_json.size();
        
        for(int i = 0; i < JSON_MAX_SIZE; i++)
        {
            JSONObject bytecode_element = (JSONObject) bytecode_json.get(i);
            String bytecode_name = (String)bytecode_element.get(ATTRIBUTE);
            bytecode_details_map.put(bytecode_name, i);    
        }
    }    
    
    private void parseConstantPoolData()
    {
        Scanner sc = new Scanner(getClass().getResourceAsStream(FILE_PATH));
                
        while(sc.hasNext())
        {
            String word = sc.next();
            
            if(word.equals("Methodref"))
            {
                sc.next(); //Skip next 2 tokens
                sc.next(); //
                
                String method_reference = sc.next();
                
                method_area.getConstantPool().add(method_reference);
                
            }
        }
    }
    
    private void extractClassName(String FQN)
    {
        String FQN_parts[] = FQN.split("\\.");
        
        int last_part_index = FQN_parts.length - 1;
            
        String class_name = FQN_parts[last_part_index];        
    }
    
    private String findFQN()
    {
        Scanner sc = new Scanner(getClass().getResourceAsStream(FILE_PATH));
        
        String first_keyword = "public";
        String second_keyword = "class";
        
        String FQN = null;
        String class_name = null;
        
        boolean FQN_found = false;
                        
        while(sc.hasNext() || !FQN_found)
        {
            String word = sc.next();
            
            if(word.equals(first_keyword))
            {
                word = sc.next();
                
                if(word.equals(second_keyword))
                {
                    FQN = sc.next();
                    FQN_found = true;
                }
            }
        }
        
        return FQN;
    }
    
    private ArrayList<Boolean> checkInstanceMethod(ArrayList<String> method_names)
    {
        ArrayList<Boolean> is_instance_method = new ArrayList<Boolean>();
        
        String FQN = findFQN();
        
        for(int i = 0; i < NUMBER_OF_METHODS; i++)
        {
            String method_name = method_names.get(i);
            
            if(method_name.contains(FQN))
            {
                is_instance_method.add(true);
            }
            else
            {
                is_instance_method.add(false);
            }
        }
                
        return is_instance_method;
    }
    
    private ArrayList<String> findMethodNames()
    {
        Scanner sc = new Scanner(getClass().getResourceAsStream(FILE_PATH));
        
        ArrayList<String> method_names = new ArrayList<String>();
        
        String first_keyword = "public";
        String second_keyword = "private";
        String remove_keyword = "class";
        
        //DOES NOT WORK WITH OBJECTS THAT HAVE FIELDS !!!!!!!!!!!!!!!!!!!!!
        
        while(sc.hasNext())
        {
            String word = sc.next();
            
            if(word.equals(first_keyword) || word.equals(second_keyword))
            {    
                String method_name = word + sc.nextLine();
                
                if(!method_name.contains(remove_keyword)) // Ensure no class names are in list
                {      
                    @SuppressWarnings("ReplaceStringBufferByString")
                    StringBuilder sb = new StringBuilder(method_name);
                    
                    sb.deleteCharAt(method_name.indexOf(";"));
                    
                    method_name = sb.toString();
                    
                    String[] method_parts = method_name.split(" ");
                                        
                    int method_name_index = method_parts.length - 1;
                    
                    method_name = method_parts[method_name_index];
                    
                    method_names.add(method_name);
                }
                
            }
            else
            {
                sc.nextLine();
            }
        }
                
        return method_names;
    }
    
    private ArrayList<String> findMethodAccess()
    {
        Scanner sc = new Scanner(getClass().getResourceAsStream(FILE_PATH));
        
        ArrayList<String> method_flags = new ArrayList<String>();
        
        String first_keyword = "public";
        String second_keyword = "private";
        String third_keyword = "protected";
        
        String remove_keyword = "class";
        
        while(sc.hasNext())
        {
            String word = sc.next();
            
            if(word.equals(first_keyword) || word.equals(second_keyword) || word.equals(third_keyword)) 
            {    
                String method_name = word + sc.nextLine();
                
                if(!method_name.contains(remove_keyword)) // Ensure no class names are in list
                {       
                    method_flags.add(word);
                }
                
            }
            else
            {
                sc.nextLine();
            }
        }     
        
        
        return method_flags;
    }
    
    private void findInstanceMethod(String class_name)
    {
        Scanner sc = new Scanner(getClass().getResourceAsStream(FILE_PATH));
        
        while(sc.hasNext())
        {
            
        }
    }
    
    /**
     * Parse through entire javap file. Look for keyword 'Code:'. This indicates
     * the start of the method information the simulator specifically needs.
     * Extract all words from this section into an arraylist. Stop extract when
     * keyword 'LineNumberTable:' occurs. As there may be more than one method 
     * in the class, use occurences to skip previous methods found.
     * @param occurences occurences of 'Code:' before begin extract
     * @return method_details ArrayList<String> Containing method data
     */
    private ArrayList<String> parseMethod(int occurences)
    {
        Scanner sc = new Scanner(getClass().getResourceAsStream(FILE_PATH));
        
        ArrayList<String> method_details = new ArrayList<String>();
        
        final String START_KEYWORD = "Code:";
        final String END_KEYWORD = "LineNumberTable:";
        
        int current_occurences = 0;
        
        boolean start = false;
        boolean end = false;
        
        while(sc.hasNext() && !end)
        {
            String word = sc.next();
            
            if(word.equals(START_KEYWORD))
            {                                
                if(current_occurences == occurences)
                {
                    start = true;
                }
                
                current_occurences++;
            }
            
            if(word.equals(END_KEYWORD) && start)
            {
                end = true;
            }
            
            if(start)
            {
                method_details.add(word);
            }
        }
        
        return method_details;
    }
    
    private void extractNumberOfMethods()
    {
        Scanner sc = new Scanner(getClass().getResourceAsStream(FILE_PATH));
        
        int method_count = 0;
                              
        while(sc.hasNext())
        {
            String word = sc.next();
            
            if(word.equals("Code:"))
            {
                method_count++;
            }            
        }                

        NUMBER_OF_METHODS = method_count;        
    }
       
    public int getNumberOfMethods()
    {
        return NUMBER_OF_METHODS;
    }
    
    public HashMap getByteCodeDetails()
    {
        return bytecode_details_map;
    }
    
    public ArrayList<Method> getMethods()
    {
        return methods;
    }
}
