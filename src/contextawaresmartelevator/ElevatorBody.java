package contextawaresmartelevator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLOutput;
import java.time.LocalTime;

import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class Person{
    String name;
    long houseID;
    long carID;
    long floor;
    JSONObject ConfigProf;
}

class Camera{
    int state;
    String resItems;
    static int cloudID;
    int floor;
    long count;

    Camera(int flo){
        floor = flo;
        state = 1;
        count = 0;
    }
}

public class ElevatorBody
{
    static int state;
    static Vector<Integer> vibState; // 100
    static Vector<Integer> vibration;

    static String strTime;
    static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    static Date d;

    static Date date;
    static DateFormat dateFor = new SimpleDateFormat("yyyy-MM-dd");
    static String strDate;

    static int wtSenID;  // 101
    static int wtSenState;
    static int extWeight;
    static int sysWt = 100; //kg
    static int wtThresh = 1100; //kg
    static int wtSus = 0;

    static Vector<Integer> motorID; // 102
    static Vector<Integer> motorState;
    static int motorDirection;

    // static Vector<Integer> extinguisherID; // 103
    static Vector<Integer> extinguisherState; // 103

    static long presentLocation = 0;
    static long nextLocation = 0;
    static long selectedLocation = 0;

    static Vector<Integer> tempState; // 104
    static Vector<Integer> temperature;

    static int airConditionerID;  // 105
    static int airConditionerSetTemp;

    static long floorSensorID; // 106
    static int FCount = 0;

    static Alarm alarm;

    static Vector<Camera> cam; // 888

    static Maintenance M;
    static LinkedHashMap<String,Person> per = new LinkedHashMap<>(); //999
    static JSONParser jsonParser = new JSONParser();

    static NetworkAdaptor net = new NetworkAdaptor();

    static Scanner SysConsole = new Scanner(System.in);

    public static void main(String[] args) {

        System.out.println("Elevator Powered ON...");
        //ContextProcessor MyCP = new ContextProcessor();
        state = 1;
        alarm = new Alarm();

        M = new Maintenance();

        // 5 vibration sensors for 5 floors
        vibState = new Vector<>();vibration = new Vector<>();
        vibState.add(1);vibration.add(0);
        vibState.add(1);vibration.add(0);
        vibState.add(1);vibration.add(0);
        vibState.add(1);vibration.add(0);
        vibState.add(1);vibration.add(0);

        // 5 extinguishers sensors for 5 floors
        extinguisherState = new Vector<>();
        extinguisherState.add(0);
        extinguisherState.add(0);
        extinguisherState.add(0);
        extinguisherState.add(0);
        extinguisherState.add(0);


        // Let's say there are 6 cameras for 5 floors and 1 inside cabin
        cam = new Vector<>();
        cam.add(new Camera(0));
        cam.add(new Camera(1));
        cam.add(new Camera(2));
        cam.add(new Camera(3));
        cam.add(new Camera(4));
        cam.add(new Camera(100)); // inside cabin

        //Let's say there are 6 temperature sensors for 5 floors and 1 inside cabin
        tempState = new Vector<>();temperature = new Vector<>();
        tempState.add(1);temperature.add(29);
        tempState.add(1);temperature.add(29);
        tempState.add(1);temperature.add(29);
        tempState.add(1);temperature.add(29);
        tempState.add(1);temperature.add(29);
        tempState.add(1);temperature.add(29); // inside cabin
        tempState.add(1);temperature.add(0);// last one is for engine


        System.out.println("Context Aware System(1) or Context Quieryng(0) : ");
        String path = "data/context.json";
        if(SysConsole.nextInt() == 1) {
            // must be in while loop
            executeData(path);
        }
        else{
            JSONArray contextList = null;
            try(FileReader reader = new FileReader(path)) {
                Object obj = jsonParser.parse(reader);
                contextList = (JSONArray) obj;
            }
            catch (FileNotFoundException e) {
                System.out.println("\nUnable to find the Context file");
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                System.out.println("Cannot understand that...  Check the format again");
//                    e.printStackTrace();
            }


            System.out.println("About Context Logs : ");
            if(SysConsole.nextInt() == 1){
                try {
                    SysConsole.nextLine();
                    System.out.print("from time (YYYY-MM-dd hh:mm:ss) : ");

                    String from = SysConsole.nextLine();
                    Date f = dateFormat.parse(from);

                    System.out.print("\nto time :  ");

                    String to = SysConsole.nextLine();
                    Date t = dateFormat.parse(to);

                    assert contextList != null;
                    for (Object i : contextList) {
                        JSONObject ci = (JSONObject) i;
                        String cTime = (String) ci.get("t");
                        cTime = dateFor.format(f) + " " + cTime;
                        d = dateFormat.parse(cTime);
                        if (d.after(f) && d.before(t)) {
                            System.out.println(ci.toJSONString());
                        }
                    }
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println("About People arrivals : ");
                if(SysConsole.nextInt() == 1){
                    try {
                        SysConsole.nextLine();
                        System.out.print("from time (YYYY-MM-dd hh:mm:ss) : ");

                        String from = SysConsole.nextLine();
                        Date f = dateFormat.parse(from);

                        System.out.print("\nto time :  ");

                        String to = SysConsole.nextLine();
                        Date t = dateFormat.parse(to);

                        assert contextList != null;
                        for (Object i : contextList) {
                            JSONObject ci = (JSONObject) i;
                            String cTime = (String) ci.get("t");
                            cTime = dateFor.format(f) + " " + cTime;
                            d = dateFormat.parse(cTime);
                            if (d.after(f) && d.before(t) && ci.get("ID")!=null && (long)ci.get("ID") == 100888 && ci.get("info")!=null &&!ci.get("info").equals("alert")) {
                                if(ci.get("info")!=null) {
                                    if (ci.get("info").equals("person")) {
                                        System.out.println("Person : " + ci.get("person") + " at time " + ci.get("t"));
                                    }
                                    if (ci.get("info").equals("multiple")) {
                                        System.out.println("Multiple people arrived at time " + ci.get("t"));
                                    }
                                }
                            }
                        }
                    }
                    catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    System.out.println("About temperature records: ");
                    if(SysConsole.nextInt() == 1) {
                        try {
                            SysConsole.nextLine();
                            System.out.print("from time (YYYY-MM-dd hh:mm:ss) : ");

                            String from = SysConsole.nextLine();
                            Date f = dateFormat.parse(from);

                            System.out.print("\nto time :  ");

                            String to = SysConsole.nextLine();
                            Date t = dateFormat.parse(to);

                            assert contextList != null;
                            for (Object i : contextList) {
                                JSONObject ci = (JSONObject) i;
                                String cTime = (String) ci.get("t");
                                cTime = dateFor.format(f) + " " + cTime;
                                d = dateFormat.parse(cTime);
                                if (d.after(f) && d.before(t) && ci.get("ID") != null && (long) ci.get("ID") % 1000 == 104 && ci.get("Temp") != null) {
                                    System.out.println("Temperature at time " + ci.get("t") + " is " + ci.get("Temp"));
                                }
                            }
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        System.out.println("Currently the system doesn't support other kinds of Queries...");
                    }
                }

            }
        }

    }

    private static void executeData(String s){
        if(state == 1) {
            try (FileReader reader = new FileReader(s)) {
                //Delete context file
                //Read JSON file
                Object obj = jsonParser.parse(reader);


                JSONArray contextList = (JSONArray) obj;
                //System.out.println(contextList);

                date = Calendar.getInstance().getTime();
                strDate = dateFor.format(date);

                contextList.forEach(i -> doReqOperation((JSONObject) i));


            } catch (FileNotFoundException e) {
//                e.printStackTrace();
                System.out.println("\nUnable to find the Context file");

            }
            catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Ignoring Context, Elevator Powered Off...");
        }
    }



    private static void doReqOperation(JSONObject a) {
        long ID;int ind,device;

        try {
            ID = (long) a.get("ID");
            strTime = (String) a.get("t");

            strTime = strDate + " " + strTime;
            //System.out.println(ID);
            ind = (int) ID / 1000;
            device = (int) ID % 1000;
            d = dateFormat.parse(strTime);
        }
        catch (Exception e){
            System.out.println("\nBasic Configuration Missing... <format not supported>");
            return;
        }

        //System.out.println(device);

        switch (device) {
            case 0:
                System.out.println("\nAlarm Pressed");
                alarm.toggle();
                break;
            case 100:
                System.out.println("\nThis is from Vibration Sensor");
                long freq = (long) a.get("Freq");
                System.out.println("Ind : " + ind);
                if(freq > 1000) {
                    System.out.println("Abnormal Vibrations detected at floor " + ind);
                    vibState.set(ind, 1);
                    vibration.set(ind, (int) freq);
                    System.out.println("Calling Maintenance Team");
                    net.send("Abnormal Vibrations detected at floor " + ind + " <at time " + d + ">");
                    M.call("Abnormal vibrations in floor " + ind + " <at time " + d + ">");
                }
                if(wtSus == 1){
                    if(freq > 500){
                        System.out.println("Suspecting Heavy Weight, Raising alarms");
                        alarm.raise(0);
                    }
                }
                break;
            case 101:
                try {
                    System.out.println("\nThis is from weight Sensor");
                    long extWt = (long) a.get("Wt") - sysWt;
                    System.out.println("External Weight : " + extWt);
                    if (extWt < wtThresh) {
                        extWeight = (int) extWt;

                        if (alarm.status == 1) {
                            alarm.down();
                        }
                    }
                    if (extWt < 0) {
                        wtSus = 1;
                        System.out.println("Uncertainty Detected!!! May be Faulty Weight Sensor");
                        net.send("Abnormal Behavior by Weight Sensor, Weight : " + extWt + " <at time "+d+">");
                        M.call("Abnormal Behavior by Weight Sensor, Weight :  " + extWt + " <at time "+d+">");
                    } else if(extWt > wtThresh) {
                        System.out.println("Too Heavy, Raising alarms");
                        alarm.raise(0);
                    }
                } catch (Exception e) {
                    System.out.println("Unexpected Behavior from Weight system... Reporting to Maintenance");
                    net.send("Abnormal Behavior by Weight System" + " <at time "+d+">");
                    M.call("Abnormal Behavior by Weight System" + " <at time "+d+">");
                }
                break;
            case 102:
                System.out.println("\nThis is from Motor");
                break;
            case 103:
                System.out.println("\nThis is from Extinguisher");
                break;
            case 104:
                try {
                    System.out.println("\nThis is from Temp Sensor");
                    long temp = (long) a.get("Temp");
                    temperature.set(ind, (int) temp);
                    if (ind == temperature.size()-1){
                        if(temp > 300){
                            System.out.println("The Engine is over heating, stopping in the nearest floor");
                            net.send("Engine got overheated" + " <at time " + d + ">");
                            M.call("Engine got overheated" + " <at time " + d + ">");
                        }

                        // context reasoning
                        if(temp > 200){
                            Camera c = cam.get(5);
                            if(c.count > 4){
                                System.out.println("Suspecting Weight sensor <at time " + d + ">");
                                System.out.println("Stopping at the nearest floor, Powering off the Elevator");
                                net.send("Suspicious Weight sensor <at time " + d + ">");
                                M.call("Suspicious Weight sensor <at time " + d + ">");

                            }
                        }
                    }
                    else {
                        if (temp > 75) {
                            if (ind != 99)
                                System.out.println("High temperature Detected in floor " + ind);
                            else System.out.println("High temperature Detected inside the cabin");

                            System.out.println("Activating extinguishers");
                            extinguisherState.set(ind, 1);
                            System.out.println("Moving to the nearest Floor...");
                            state = 0;
                            System.out.println("Elevator Powered Off");
                        } else if (temp < 5) { // this can change based on the region
                            System.out.println("Suspecting Temperature sensor of index : " + ind + " <at time " + d + ">");
                            net.send("Suspicious Behavior by Temperature sensor" + " <at time " + d + ">");
                            M.call("Suspicious Behavior by Temperature sensor" + " <at time " + d + ">");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Abnormal Behavior from Temperature System");
                    net.send("Abnormal Behavior by Temperature System" + " <at time "+d+">");
                    M.call("Abnormal Behavior by Temperature System" + " <at time "+d+">");
                }
                break;
            case 105:
                System.out.println("\nThis is from Air Conditioner");
                break;
            case 106:  // for floor sensors
                if (ind == presentLocation || ind - presentLocation >= 2) {
                    FCount++;
                    //System.out.println(FCount);
                    if (FCount > 3) {
                        System.out.println("Uncertainty detected!!!, May be faulty floor Sensor" + " <at time "+d+">");
                        net.send("Abnormal Behavior by floor Sensor " + ind + " <at time "+d+">");
                        M.call("Abnormal Behavior by floor Sensor " + ind + " <at time "+d+">");
                    }
                } else if (ind == nextLocation) {
                    FCount = 0;
                    presentLocation = ind;
                    System.out.println("\nReached floor : " + ind);
                    motorDirection = 0;
                } else {
                    FCount = 0;
                    System.out.println("\nThis is from Floor Sensor");
                    presentLocation = ind;
                    if (nextLocation > presentLocation) {
                        motorDirection = -1;
                    } else {
                        motorDirection = 1;
                    }
                    System.out.println("The cabin is in floor : " + ind);
                    floorSensorID = ID;
                }
                break;
            case 555:
                System.out.println("\nThis is from power switch");
                if (state == 1) {
                    System.out.println("Powering off the Elevator");
                    state = 0;
                } else {
                    System.out.println("Powering on the Elevator");
                    state = 1;
                }
                break;
            case 888:
                try {
                    if (ind == 100) {
                        ind = 5;
                        Camera c = cam.get(5);
                        System.out.println("\nThis is from Internal Camera");
                        String info = (String) a.get("info");

//                        System.out.println(info);
                        if (info.equals("person")) {
                            // if the person is unknown, it will ask the details and save them
                            c.count = 1;
                            if (a.get("person").equals("unknown")) {
                                System.out.println("Do you live here??");
                                if (SysConsole.next().equals("yes")) {
                                    Person P = new Person();
                                    System.out.println("Enter your Name : ");
                                    P.name = SysConsole.nextLine();
                                    System.out.println("Enter your Floor no : ");
                                    P.floor = SysConsole.nextLong();
                                    System.out.println("Enter your House ID : ");
                                    P.houseID = SysConsole.nextLong();
                                    System.out.println("Enter your Car ID : ");
                                    P.carID = SysConsole.nextLong();
                                    System.out.println("Uploading to cloud, Your details will be asked for confirmation");
                                    per.put(P.name, P);
                                } else {
                                    selectedLocation = SysConsole.nextLong();
                                    System.out.println("Moving to floor : " + selectedLocation);
                                    nextLocation = selectedLocation;
                                    if (nextLocation > presentLocation) {
                                        motorDirection = -1;
                                        System.out.println("Moving Up");
                                    } else {
                                        motorDirection = 1;
                                        System.out.println("Moving Down");
                                    }
                                }
                            }
                            Person p2 = per.get((String) a.get("person"));
                            System.out.println("Detected person " + p2.name + " at location " + presentLocation + " <at time "+d+">");
                            if (p2.floor != presentLocation) {
                                System.out.println("Moving to floor : " + p2.floor);
                                nextLocation = p2.floor;
                            } else {
                                System.out.println("Asking to select a floor...");
                                selectedLocation = SysConsole.nextLong();
                                System.out.println("Moving to floor : " + selectedLocation);
                                nextLocation = selectedLocation;
                            }
                            if (nextLocation > presentLocation) {
                                motorDirection = -1;
                                System.out.println("Moving Up");
                            } else {
                                motorDirection = 1;
                                System.out.println("Moving Down");
                            }
                        } else if (info.equals("alert")) {
                            System.out.println("Unfavourable Situation detected, Raising alarms" + " <at time "+d+">");
                            c.resItems = (String) a.get("ResItem");
                            net.send(c.resItems + " Detected by camera " + ind + " <at time "+d+">");
                            alarm.raise(1);
                        }
                        else if(info.equals("multiple")){
                            c.count = (long) a.get("count");
                            System.out.println("Multiple Ppl in Cabin, please select your floor of choice");
                            for(int i= 0;i<c.count;i++){
                                SysConsole.nextLong();
                            }
                        }
                    } else {
                        System.out.println("\nThis is from External Camera");
                        System.out.println("Moving to floor : " + ind);
                        nextLocation = ind;
                        if (nextLocation > presentLocation) {
                            motorDirection = -1;
                            System.out.println("Moving Up");
                        } else {
                            motorDirection = 1;
                            System.out.println("Moving Down");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Unexpected Behavior From Camera... Reporting to Maintenance");
                    net.send("Abnormal Behavior by Camera System" + " <at time "+d+">");
                    M.call("Abnormal Behavior by Camera System" + " <at time "+d+">");
                }
                break;
            case 999:
                try {
                    Person p = new Person();
                    p.name = (String) a.get("name");
                    if(p.name == null){
                        System.out.println("\nInvalid Information...");
                        return;
                    }
                    System.out.println("\nThis is a " + p.name + "'s profile update");
                    p.carID = (long) a.get("carID");
                    p.houseID = (long) a.get("houseID");
                    p.ConfigProf = (JSONObject) a.get("Config");
                    p.floor = (long) a.get("floor");
                    per.put(p.name, p);

                } catch (Exception e) {
                    System.out.println("\nUnexpected Data arrived From Person Profile Update" + " <at time "+d+">");
                }
                break;
            default:
                System.out.println("\nUnable to Recognize the device..." + " <at time "+d+">");
        }
    }
}

//class ContextProcessor{
//    int extWt; // ExternalWt integer;
//    Vector<Integer> emerCode;
//    int cloudID = 91238472;
//    String netString;
//    boolean struStatus;
//    int selLoc;
//    int liftLocation;
//    Vector<Person> p;
//    int CabTemp;
//
//}

class Maintenance{
    String Prob;
    int status = 0;

    void call(String s){
        System.out.println("Received Maintenance call for "+s);
        Prob = s;
        status = 1;
    }

}

class NetworkAdaptor{
    Vector<Integer> portNo;
    String info;

    public void send(String s){
        info = s;
        System.out.println("Transmitting info :"+ s);
    }

}
class Alarm{
    int status;
    int emerCode; // 0 - heavy Weight, 1 - unfavourable situation from camera

    public void raise(int i){
        if(status != 1) {
            status = 1;
            emerCode = i;
            System.out.println("!!! Alarms Raised !!!");
        }
    }

    public void down(){
        status = 0;
        System.out.println("...Alarms Down...");
    }

    public void toggle(){
        if(status == 0){
            System.out.println("!!! Alarms Raised !!!");
            status = 1;
            return;
        }
        status = 0;
        System.out.println("...Alarms Down...");
    }
}

