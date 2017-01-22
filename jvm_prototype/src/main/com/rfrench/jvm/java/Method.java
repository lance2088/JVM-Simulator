
package main.com.rfrench.jvm.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Method 
{
    private final int HEX = 16;
    
    private ArrayList<String> parsed_method_data;
    private ArrayList<String> method_bytecode;
    private ArrayList<String> method_line_numbers;
    private ArrayList<Integer> memory_opcodes;
    
    private int MAX_STACK_SIZE;
    private int MAX_LOCAL_VAR_SIZE;
    private int MAX_ARG_SIZE;
    private int NUMBER_OPCODES_PROGRAM;
    
    private HashMap bytecode_details_map;
    private JSONArray bytecode_details_json;
       
    public Method(HashMap bytecode_details_map, JSONArray bytecode_details_json)
    {
        this.parsed_method_data = parsed_method_data;
        this.bytecode_details_map = bytecode_details_map;   
        this.bytecode_details_json = bytecode_details_json;
        
        MAX_STACK_SIZE = 0;
        MAX_LOCAL_VAR_SIZE = 0;
        MAX_ARG_SIZE = 0;
        NUMBER_OPCODES_PROGRAM = 0;
        
        method_bytecode = new ArrayList<String>();
        method_line_numbers = new ArrayList<String>();   
        memory_opcodes = new ArrayList<Integer>();
    }
    
    public Method(HashMap bytecode_details_map, JSONArray bytecode_details_json, ArrayList<String> parsed_method_data)
    {
        this.parsed_method_data = parsed_method_data;
        this.bytecode_details_map = bytecode_details_map;   
        this.bytecode_details_json = bytecode_details_json;
        
        MAX_STACK_SIZE = 0;
        MAX_LOCAL_VAR_SIZE = 0;
        MAX_ARG_SIZE = 0;
        NUMBER_OPCODES_PROGRAM = 0;
        
        method_bytecode = new ArrayList<String>();
        method_line_numbers = new ArrayList<String>();  
        memory_opcodes = new ArrayList<Integer>();
        
        setupMethod();
    }
    
    public void setupMethod()
    {
        extractByteCode();
        
        extractLineNumbers();
        
        extractMethodAttributes();
        
        extractOpcodes();
        
        for(int i = 0; i < memory_opcodes.size(); i++)
        {
            System.out.println(memory_opcodes.get(i));
        }
        
        System.out.println("*****");
    }
    
    public void setupMethod(ArrayList<String> parsed_method_data)
    {
        this.parsed_method_data = parsed_method_data;
        
        setupMethod();
    }
        
    private void extractMethodAttributes()
    {
        MAX_STACK_SIZE = extractMethodDetails("stack=");
        
        MAX_LOCAL_VAR_SIZE = extractMethodDetails("locals=");
        
        MAX_ARG_SIZE = extractMethodDetails("args_size=");
    }
    
    private void extractByteCode()
    {                               
        final int PARSED_DATA_SIZE = parsed_method_data.size();
        
        int count = 0;
        
        while(count < PARSED_DATA_SIZE)
        {
            String word = parsed_method_data.get(count).toUpperCase();
            
            if(bytecode_details_map.containsKey(word))
            {
                int bytecode_map_index = (int) bytecode_details_map.get(word);

                JSONObject bytecode_element = (JSONObject) bytecode_details_json.get(bytecode_map_index);

                final String ATTRIBUTE = "Total Operands";

                long b = (long) bytecode_element.get(ATTRIBUTE);

                int bytecode_operands = (int) b;
                                
                switch(bytecode_operands)
                {
                    case (0):
                        break;
                    case (1):
                    case (2):
                        count++;
                        word += " " + parsed_method_data.get(count);
                        break;
                    case (3):
                        count++;
                        word+= " " + parsed_method_data.get(count) ;
                        count++;
                        word+= " " + parsed_method_data.get(count);
                }
                
                method_bytecode.add(word);
            }
                            
            count++;
        }
    }
    
    private void extractLineNumbers()
    {
        final int PARSED_DATA_SIZE = parsed_method_data.size();
        
        String previous_word = null;
        
        for(int i = 0; i < PARSED_DATA_SIZE; i++)
        {
            String word = parsed_method_data.get(i).toUpperCase();
            
            if (bytecode_details_map.containsKey(word)) 
            {
                method_line_numbers.add(previous_word);
            }
            
            previous_word = word;
        }

    }
    
    private int extractMethodDetails(String keyword)
    {         
        final int PARSED_DATA_SIZE = parsed_method_data.size();
        
        int value = -1;
        
        boolean attribute_found = false;
        
        List<String> keywords = new ArrayList<String>();
        
        keywords.add(keyword);       
        
        String patternString = "\\b(" + StringUtils.join(keywords, "|") + ")\\b";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher;
              
        int count = 0;
        
        while(!attribute_found || count < PARSED_DATA_SIZE)
        {
            String word = parsed_method_data.get(count);
            
            matcher = pattern.matcher(word);
            
            if(matcher.find())
            {                
                value = Integer.parseInt(word.replaceAll("\\D+", ""));                  
                
                attribute_found = true;
            }
            
            count++;
        }               
        
        return value;
    }
    
//    private void writeByteCodeToMemory()
//    {                
//        final int MAX_BYTE_VALUE = 255;
//                
//        Pattern pattern_branch = createBranchPattern();
//        Matcher matcher_branch;
//        
//        Pattern pattern_operand = createOperandPattern();
//        Matcher matcher_operand;
//                
//        int memory_location = 0;
//                
//        for(int i = 0; i < NUMBER_OPCODES_PROGRAM; i++)
//        {      
//            String word = method_bytecode.get(i);
//            String parameter = "";
//                                 
//            matcher_branch = pattern_branch.matcher(word);
//            matcher_operand = pattern_operand.matcher(word);
//            
//            if(word.indexOf(' ') != -1)            
//            {
//               parameter = word.substring(word.indexOf(' ') + 1, word.length());
//               word = word.substring(0, word.indexOf(' '));                              
//            }            
//
//            if(bytecode_details_map.containsKey(word))
//            {
//                int bytecode_map_index = (int)bytecode_details_map.get(word); 
//                
//                JSONObject bytecode_element = (JSONObject) bytecode_details_json.get(bytecode_map_index);
//                                
//                final String ATTRIBUTE = "Opcode";
//                
//                String opcode_text = (String)bytecode_element.get(ATTRIBUTE);                
//                
//                int opcode = Integer.parseInt(opcode_text, HEX);
//                
//                memory_data.add(opcode);
//               
//                if(matcher_branch.find())
//                {
//                    int offset = Integer.parseInt(parameter);
//                    int param_1 = 0;
//                    int param_2 = offset;
//                
//                    if(param_2 > MAX_BYTE_VALUE)
//                    {                
//                        param_1 = offset - MAX_BYTE_VALUE;
//                        param_2 = 255;
//                    }
//
//                    memory_data.add(param_1);
//                    memory_data.add(param_2);
//                }
//                
//                else if(word.equals("IINC"))
//                {                    
//                    int param_1 = Integer.parseInt(parameter.substring(0, parameter.indexOf(',')));
//                    memory_data.add(param_1);
//                    int param_2 = Integer.parseInt(parameter.substring(parameter.indexOf(' ') + 1, parameter.length()));
//                    memory_data.add(param_2); 
//                }                
//                
//                if(matcher_operand.find())
//                {                   
//                    int param_1 = Integer.parseInt(parameter);
//                    memory_data.add(param_1);
//                }
//                
//            } 
//        }           
//    }    
    
    private void extractOpcodes()
    {
        final int PARSED_DATA_SIZE = parsed_method_data.size();
        
        int count = 0;
        
        while(count < PARSED_DATA_SIZE)
        {
            String word = parsed_method_data.get(count).toUpperCase();
            
            if (bytecode_details_map.containsKey(word)) 
            {
                int bytecode_map_index = (int) bytecode_details_map.get(word);

                JSONObject bytecode_element = (JSONObject) bytecode_details_json.get(bytecode_map_index);

                final String ATTRIBUTE = "Opcode";

                String bytecode = (String) bytecode_element.get(ATTRIBUTE);
               
                memory_opcodes.add(Integer.parseInt(bytecode, HEX));
                
                if(checkBranchOpcode(word, count))
                {
                    count++;
                }
                
                if(checkStackOpcode(word, count))
                {
                    count ++;
                }
                
                if(checkIINCOpcode(word, count))
                {
                    count += 2;
                }
            }
            
            
            count++;
        }
    }        
          
    private boolean checkStackOpcode(String word, int index)
    {
        boolean stack_opcode = false;
        
        List<String> operand_keywords = new ArrayList<String>();
        
        operand_keywords.add("BIPUSH");
        operand_keywords.add("ILOAD");
        operand_keywords.add("ISTORE");
        
        String pattern_string_operand = "\\b(" + StringUtils.join(operand_keywords, "|") + ")\\b";
        
        Pattern pattern = Pattern.compile(pattern_string_operand);
        
        Matcher matcher = pattern.matcher(word);
        
        if(matcher.find())
        {
            int operand_index = index + 1;                        
                                    
            int operand = Integer.parseInt(parsed_method_data.get(operand_index), HEX);
            
            memory_opcodes.add(operand);
            
            stack_opcode = true;
        }
                
        return stack_opcode;
    }
    
    private boolean checkIINCOpcode(String word, int index)
    {
        boolean iinc_opcode = false;
        
        if(word.equals("IINC"))
        {
            int operand_index = index + 1;
                        
            String operand_string = parsed_method_data.get(operand_index);
            
            operand_string = operand_string.substring(0, operand_string.indexOf(',')); //Remove Comma from String
            
            int operand = Integer.parseInt(operand_string, HEX);
            
            memory_opcodes.add(operand);
            
            operand_index = index + 2;
            
            operand = Integer.parseInt(parsed_method_data.get(operand_index), HEX);
            
            memory_opcodes.add(operand);            
        }
                        
        return iinc_opcode;
    }
    
    private boolean checkBranchOpcode(String word, int index)
    {
        final int MAX_BYTE_VALUE = 255;
        
        boolean branch_opcode = false;
        
        List<String> branch_keywords = new ArrayList<String>();
        
        branch_keywords.add("IFEQ");
        branch_keywords.add("IFLT");
        branch_keywords.add("IF_ICMPGQ");  
        branch_keywords.add("IF_ICMPGE");
        branch_keywords.add("IF_ICMPNE");
        branch_keywords.add("IF_ICMPEQ");
        branch_keywords.add("GOTO");        
        
        String pattern_string_branch = "\\b(" + StringUtils.join(branch_keywords, "|") + ")\\b";
        
        Pattern pattern = Pattern.compile(pattern_string_branch);
        
        Matcher matcher = pattern.matcher(word);
        
        if(matcher.find())
        {
            int operand_index = index + 1;
            
            int offset = Integer.parseInt(parsed_method_data.get(operand_index));

            int param_1 = 0;          
            int param_2 = offset;

            if (param_2 > MAX_BYTE_VALUE)
            {
                param_1 = offset - MAX_BYTE_VALUE;                
                param_2 = MAX_BYTE_VALUE;
            }
            
            memory_opcodes.add(param_1);            
            memory_opcodes.add(param_2);
            
            branch_opcode = true;
        }
        
        
        return branch_opcode;
    }
    
    public final int getStackSize()
    {
        return MAX_STACK_SIZE;
    }
    
    public final int getLocalSize()
    {
        return MAX_LOCAL_VAR_SIZE;
    }
    
    public final int getArgSize()
    {
        return MAX_ARG_SIZE;
    }
}
