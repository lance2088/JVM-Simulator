
package controller.com.rfrench.jvm.java;

import java.util.ArrayList;
import java.util.Stack;
import javafx.scene.control.ListView;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import ui.com.rfrench.jvm.java.MainScene;

/*
    Program Title: MainSceneController.java
    Author: Ryan French
    Created: 08-Nov-2016
    Version: 1.0
*/

public class MainSceneController 
{
    private MainScene main_scene;
    
    private int stack_pane_size;
    
    private Stack button_presses_per_method;
    
    public MainSceneController(MainScene m)
    {                
        main_scene = m;
        
        stack_pane_size = 0;
        
        button_presses_per_method = new Stack();
        
        button_presses_per_method.push(-1);
    }
    
    public MainScene getMainScene()
    {
        return main_scene;
    }
    
    public void hightlightLine(int current_method, int button_press_count)
    {
        ListView current_listview = main_scene.getAssembly().getCurrentListView(current_method);
        
        current_listview.getSelectionModel().select(button_press_count);
        
        
    }
    
    public void updateRegisterLabels(int PC)
    {
        String PC_string;
        
        if(PC < 16)
            PC_string = "PC  : 0x0" + Integer.toHexString(PC).toUpperCase();
        else
            PC_string = "PC  : 0x" + Integer.toHexString(PC).toUpperCase();
        
        main_scene.getRegister().updateLabel(PC_string, 1);
    }   
    
    public Stack getButtonStack()
    {
        return button_presses_per_method;
    }
    
    public void removeAllStack()
    {
        for(int i = 0; i <= stack_pane_size; i++)
        {
            main_scene.getStack().pop(i);
        }
        
        stack_pane_size = 0;
    }
    
    public void BIPUSH(String value)
    {
        ++stack_pane_size;
        main_scene.getStack().push(value, stack_pane_size); 
    }
    
    public void ALOAD_0(String value)
    {
        ++stack_pane_size;
        main_scene.getStack().push(value, stack_pane_size);        
    }
    
    public void ICONST(String value)
    {
        ++stack_pane_size;
        main_scene.getStack().push(value, stack_pane_size); 
    }
    
    public void ILOAD(int frame_index)
    {
        ++stack_pane_size;
        main_scene.getStack().push(main_scene.getFrame().getFrameName(frame_index), stack_pane_size); 
        
    }
    
    public void IADD()
    {
        String label_name = main_scene.getStack().getStackText(stack_pane_size);          
        main_scene.getStack().pop(stack_pane_size);
        --stack_pane_size;
        
        label_name = main_scene.getStack().getStackText(stack_pane_size) + " + " + label_name;              
        main_scene.getStack().pop(stack_pane_size);                              
        --stack_pane_size;
        
        ++stack_pane_size;
        main_scene.getStack().push(label_name, stack_pane_size);
    }
    
    public void ISTORE(int frame_index, String frame_value)
    {
        main_scene.getStack().pop(stack_pane_size);     
        --stack_pane_size;

        main_scene.getFrame().updateFrameLabel(frame_index, frame_value);
        
    }
    
    public void ISUB()
    {
        String label_name = main_scene.getStack().getStackText(stack_pane_size);          
        main_scene.getStack().pop(stack_pane_size);
        --stack_pane_size;
        
        label_name = main_scene.getStack().getStackText(stack_pane_size) + " - " + label_name;              
        main_scene.getStack().pop(stack_pane_size);                              
        --stack_pane_size;
        
        ++stack_pane_size;
        main_scene.getStack().push(label_name, stack_pane_size);
    }
    
    public void IF_ICMPEQ()
    {
        main_scene.getStack().pop(stack_pane_size);
        --stack_pane_size;
        main_scene.getStack().pop(stack_pane_size);
        --stack_pane_size;        
    }
    
    public void GOTO(int offset, ArrayList<String> Linenumbers)
    {
        System.out.println("offset: " + offset);
        
        String offset_string = Integer.toString(offset);
        
        boolean line_found = false;
                
        int count = 0;
        
        while(!line_found || count > Linenumbers.size())
        {
            String next_line_number = Linenumbers.get(count);
            
            if(next_line_number.contains(offset_string))
            {
                System.out.println("Line to branch to is: " + count);
                
                button_presses_per_method.pop();
                button_presses_per_method.push(count);
                
                line_found = true;
                
            }
            
            count++;
        }

    }
    
    public void POP()
    {
        main_scene.getStack().pop(stack_pane_size);
        --stack_pane_size;   
    }
    
    public void IINC(int frame_index, String frame_value)
    {        
        main_scene.getFrame().updateFrameLabel(frame_index, frame_value);
    }
    
    public void INVOKESPECIAL(int stack_size, int current_method, int max_local_var)
    {        
        String test = "";
        
        if(stack_pane_size > 0)
        {
            test = main_scene.getStack().getStackText(stack_pane_size);
        }
                    
        for(int i = 0; i < stack_size; i++)
        {
            main_scene.getStack().pop(stack_pane_size);            
            --stack_pane_size;            
        }
        
        for(int i = 0; i < max_local_var; i++)
        {
            if(i == 0)
            {                
                main_scene.getFrame().addFrameUI(i, test, current_method);
            }
            
            else
            {
                main_scene.getFrame().addFrameUI(i, "", current_method);
            }                                
        }
                        
        SingleSelectionModel<Tab> selection_model = main_scene.getAssembly().getTabPane().getSelectionModel();
            
        selection_model.select(current_method);        
    }
    
    public void RETURN(int current_method, int max_local_var)
    {
        SingleSelectionModel<Tab> selection_model = main_scene.getAssembly().getTabPane().getSelectionModel();
            
        selection_model.select(current_method);
        
        for(int i = 0; i < max_local_var; i++)
        {
            main_scene.getFrame().removeFrameUI(i);
        }
        
      
    }
}
