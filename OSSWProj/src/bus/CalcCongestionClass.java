package bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omg.CORBA.BooleanSeqHelper;


/**
 * CalcCongestionClass
 */
public class CalcCongestionClass {
    private Boolean status;
    private BusInfoClass busInfo = BusInfoClass.getInstance();
    private HashMap<String, CongestinoClass> congestionHashMap;
    /**
     * 특정 정류장, 특정노선, 특정요일, 시간별로 승차&하차 인원수 계산
     * <station_route명, HashMap<(0평,1토,2일),Double[][]>
     * Double[][] == 시간대별로 승차, 하차인원수 계산 ==> [0][i]승차, [1][i]하차
     */
    HashMap<String,HashMap<Integer,Double[][]>> dayPassengerNum_getOnOff=new HashMap<String,HashMap<Integer,Double[][]>>();
    /**
     * 특정 정류장, 특정노선, 특정요일, 시간별(24시간*60분)로 재차인원수 계산
     * <station_route명, HashMap<(0평,1토,2일),Double[1440]>
     */
    HashMap<String,HashMap<Integer,Double[]>> passengerNum=new HashMap<String,HashMap<Integer,Double[]>>();
    /**
     * 특정 정류장, 특정노선, 특정요일, 시간별(24시간*60분)로 혼잡도 계산
     * -1 : 앉을 수 있음
     * 0 : 좌석은 찼는데 손잡이는 남음
     * 1 : 손잡이 넘음
     * <station_route명, HashMap<(0평,1토,2일),Double[1440]>
     */
    HashMap<String,HashMap<Integer,int[]>> finalCongestion=new HashMap<String,HashMap<Integer,int[]>>();
        
    /**
     * constructor for calculate congestion
     */
    public CalcCongestionClass(boolean status){
        if(status){
            this.congestionHashMap=busInfo.getCongestionHashMap();
            if(calc_getOnOff()){
                if(calc_Passenger()){
                    this.status=calc_congestion();
                }else{
                    System.out.println("CalcCongestionClass : 재차인원 계산 실패");
                }
            }else{
                System.out.println("CalcCongestionClass : 승,하차 인원 계산 실패");
            }
        }
    }

    /**
     * 특정 정류장, 특정 노선, 특정 요일, 시간별로 승차, 하차 인원수 계산
     * @return 수행성공여부
     */
    private boolean calc_getOnOff(){
        boolean status=true;
        Iterator<String> iter=this.congestionHashMap.keySet().iterator();
        while(iter.hasNext()){
            String stationID_routeName=(String)iter.next();
            CongestinoClass tmp=this.congestionHashMap.get(stationID_routeName);

            //0평일, 1토요일, 2일요일
            HashMap<Integer,Double[][]> days=new HashMap<Integer,Double[][]>();
            for(int day=0;day<3;day++){
                //승차, 하차인원수 계산 ==> [0][i]승차, [1][i]하차
                Double[][] gettingOn_Off=tmp.calcGettingOnAndOff(day);
                days.put(day,gettingOn_Off);
            }
            dayPassengerNum_getOnOff.put(stationID_routeName, days);
        }
        return true;
    }

    /**
     * 특정 정류장, 특정 노선 특정 요일 시간별로(24시간*60분) 재차인원수 계산
     * @return
     */
    private boolean calc_Passenger(){
        boolean status=true;

        Iterator<String> iter=this.congestionHashMap.keySet().iterator();
        while(iter.hasNext()){
            String stationID_routeName=(String)iter.next();

            //0평일, 1토요일, 2일요일별로 저장
            HashMap<Integer,Double[]> days=new HashMap<Integer,Double[]>();
            for(int day=0;day<3;day++){
                // 이전정류장의 버스재차인원 + 승차인원 - 하차인원
                Double[] minutes=new Double[1440];
                for(int i=0;i<1440;i++){
                    double before_passenger=getBeforePassengerNum(day,i,stationID_routeName);
                    minutes[i]=before_passenger 
                                +this.dayPassengerNum_getOnOff.get(stationID_routeName).get(day)[0][i]
                                -this.dayPassengerNum_getOnOff.get(stationID_routeName).get(day)[1][i];
                }
                days.put(day,minutes);
            }
            passengerNum.put(stationID_routeName,days);
        }
        return status;
    }

    /**
     * 이전 정류장 들의 재차인원을 구해야 함!
     * @param nowTime
     * @param stationID_routeName
     * @return
     */
    private Double getBeforePassengerNum(int day,int nowTime, String stationID_routeName){
        Double value=0.0;

        CongestinoClass nowCongestion=congestionHashMap.get(stationID_routeName);
        String stationID=nowCongestion.getStationID();
        String routeName=nowCongestion.getrouteName();

        //nowTime 시간에 승하차 인원이 없다면 0 반환
        //예) nowTime=3시7분 ==> 시간대=3-4시 ==> 승차=0,하차=0이면 승객이 없으므로 False
        if(!nowCongestion.IsPassengerExist((int)nowTime/60)){
            return 0.0;
        }
        String before_stationID=getBeforeStation(routeName,stationID);//a->b->c->.....y->z->a->b...
        int timeInterval=getTimeInterval(routeName,stationID,before_stationID);
        value=getOnPassenger(day,(int)nowTime/60,nowTime%60,before_stationID+"__"+routeName)
            -getOffPassenger(day,(int)nowTime/60,nowTime%60,before_stationID+"__"+routeName)
            +getBeforePassengerNum(day,nowTime-timeInterval, before_stationID+"__"+routeName);
        
        return value;
    }

    /**
     * 노선에서 이전 정류장의 stationID를 반환
     * 시점의 이전 정류장은 종점을 말한다 ==> % 사용
     * @param routeName 
     * @param stationID 
     */
    private String getBeforeStation(String routeName, String stationID){
        String result="";
        RouteClass route=busInfo.getRouteInfo(routeName);
        int idx=route.stationList.indexOf(stationID);
        result=route.stationList.get((idx-1)%route.stationList.size());
        return result;
    }

    /**
     * stationID정류장과 before_stationID정류장 사이의 시간간격을 반환
     * @param routeName
     * @param stationID
     * @param before_stationID
     * @return
     */
    private int getTimeInterval(String routeName, String stationID,String before_stationID){
        int result=5;//여기도 임의로 테스트용으로 5분간격이라고 한거임!

        //희수 코드와 합쳐야 함

        return result;
    }

    
    /**
     * 원하는 시간대를 쪼개 임의로 hh시 mm분에 대한 승차인원 계산
     * 수식 검사 후 수정 여부 판단.
     * @param day : 평일, 토요일, 일요일 중 입력
     * @param hour : 0~23시
     * @param minute : 0~59분
     * @return hh시 mm분일 때 버스의 승차인원
     */
    private Double getOnPassenger(int day, int hour, int minute,String stationID_routeName){
        return (this.dayPassengerNum_getOnOff.get(stationID_routeName).get(day)[0][(hour+1)%24]
                - this.dayPassengerNum_getOnOff.get(stationID_routeName).get(day)[0][(hour)%24])
                * (minute/60) 
                +this.dayPassengerNum_getOnOff.get(stationID_routeName).get(day)[0][(hour)%24];
     } 

     /**
     * 원하는 시간대를 쪼개 임의로 hh시 mm분에 대한 하차인원 계산
     * 수식 검사 후 수정 여부 판단.
     * @param day : 평일, 토요일, 일요일 중 입력
     * @param hour : 0~23시
     * @param minute : 0~59분
     * @return hh시 mm분일 때 버스의 하차인원
     */
    private Double getOffPassenger(int day, int hour, int minute,String stationID_routeName){
        return (this.dayPassengerNum_getOnOff.get(stationID_routeName).get(day)[1][(hour+1)%24]
                - this.dayPassengerNum_getOnOff.get(stationID_routeName).get(day)[1][(hour)%24])
                * (minute/60) 
                +this.dayPassengerNum_getOnOff.get(stationID_routeName).get(day)[1][(hour)%24];
     } 

    private boolean calc_congestion(){
        boolean status=true;
        Iterator<String> iter=this.congestionHashMap.keySet().iterator();
        while(iter.hasNext()){
            String stationID_routeName=(String)iter.next();
            ArrayList<Integer> chair_handle=getBusType(stationID_routeName);

            //0평일, 1토요일, 2일요일별로 저장
            HashMap<Integer,int[]> days=new HashMap<Integer,int[]>();
            for(int day=0;day<3;day++){

                // 24시간*60분 별로 저장
               int[] minutes=new int[1440];
                for(int i=0;i<1440;i++){
                    double num=passengerNum.get(stationID_routeName).get(day)[i];
                    //좌석에 앉을 수 있으면 -1
                    //손잡이를 잡을 수 있으면 0
                    //둘다 불가능이면 1
                    if(num < chair_handle.get(0)){
                        minutes[i]=-1;
                    }else if(num < chair_handle.get(0)+chair_handle.get(1)){
                        minutes[i]=0;
                    }else{
                        minutes[i]=1;
                    }
                }
                days.put(day,minutes);
            }
            finalCongestion.put(stationID_routeName,days);
        }
        return status;
    }

    /**
     * 
     * @param routeName
     * @return 버스 종류 
     * 광역버스(빨강) - 숫자4자리, 9로 시작                  45/0 ex)구글링+뇌피셜
     * 지선버스(초록) - 숫자4자리           좌석20 손잡이17  ex)5511번 버스 조사
     * 간선버스(파랑) - 숫자3자리           좌석24 손잡이24  ex)151번 버스 조사
     * 순환버스(노랑) - 숫자2자리                           20/17 ex)구글링+뇌피셜
     * 마을버스         한글로 시작+숫자    좌석20 손잡이17  ex)동작01번 버스 조사
     * N버스            N으로 시작+숫자                     24/24 ex)구글링+뇌피셜
     */
    private ArrayList<Integer> getBusType(String stationID_routeName){
        String routeName=stationID_routeName.substring(stationID_routeName.indexOf("___")+3);
        
        Pattern pD4_9=Pattern.compile("^9[0-9]{3}");
        Pattern pD4=Pattern.compile("^[0-9]{4}");
        Pattern pD3=Pattern.compile("^[0-9]{3}");
        Pattern pD2=Pattern.compile("^[0-9]{2}");
        Pattern pHD=Pattern.compile("^[가-힣]*[0-9]{2}");
        Pattern pN=Pattern.compile("^N");

        ArrayList<Integer> result=new ArrayList<>();
        if(pD4_9.matcher(routeName).find()){
            System.out.println("광역");
            result.add(45);
            result.add(0);

        }else if(pD4.matcher(routeName).find()){
            System.out.println("지선");
            result.add(20);
            result.add(17);

        }else if(pD3.matcher(routeName).find()){
            System.out.println("간선");
            result.add(24);
            result.add(24);

        }else if(pD2.matcher(routeName).find()){
            System.out.println("순환");
            result.add(20);
            result.add(17);

        }else if(pHD.matcher(routeName).find()){
            System.out.println("마을");
            result.add(20);
            result.add(17);

        }else if(pN.matcher(routeName).find()){
            System.out.println("N심야");
            result.add(24);
            result.add(24);

        }else{
            System.out.println("알수없는 형식의 버스번호입니다.");
            result.add(-1);
            result.add(-1);
        }
        return result;
    }
}