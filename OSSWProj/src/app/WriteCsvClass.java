package app;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;

import bus.RouteClass;
import bus.StationClass;

//csv파일 만들기 ,미완성
public class WriteCsvClass {

    public WriteCsvClass(){
        
    }

    public void writeRouteClass(HashMap<String, RouteClass> routelist) throws IOException {
       
        try {

            String csvfilename = "C:/Users/yoonhee/Desktop/ossproject/DataParsing/routecsv.csv";
            
            BufferedWriter fw;

            fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvfilename), Charset.forName("EUC-KR")));
            
            Collection<RouteClass> values = routelist.values();
       
            for(RouteClass value : values){
            
                fw.write(value.getRouteId()+","+value.getRouteName()+",");

                for(int i = 0 ; i < value.getStationList().size() ; i++){
                    fw.write(value.getStationList().get(i) + ",");
                }

                fw.newLine();
            }
            fw.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

       
    }

    public void writeStationClass(HashMap<String, StationClass> stationlist){

    }

    
}