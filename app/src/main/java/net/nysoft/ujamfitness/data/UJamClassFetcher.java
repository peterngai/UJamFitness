package net.nysoft.ujamfitness.data;

import android.util.Log;

import java.util.Iterator;
import java.util.LinkedHashMap;


/**
 * TODO: Write Javadoc for MockUJamClasses.
 *
 * @author pngai
 */
public class UJamClassFetcher implements UJamJSONParser.OnTaskCompleted {

    protected String TAG = getClass().getName();

    protected LinkedHashMap<Integer, UJamGym> _gymList = new LinkedHashMap<Integer, UJamGym>();
    protected Integer _selectedGym;
    protected UJamJSONParser _jsonParser;
    protected UJamJSONParser.OnTaskCompleted _listener;

    public UJamClassFetcher(UJamJSONParser.OnTaskCompleted listener) {
        _listener = listener;
    }

    public LinkedHashMap<Integer, UJamGym> get_gymList() {
        return _gymList;
    }

    public Integer get_selectedGym() {
        return _selectedGym;
    }

    public void set_selectedGym(Integer _selectedGym) {
        this._selectedGym = _selectedGym;
    }

    public void invokeGymFetch() {

        _jsonParser = new UJamJSONParser(this);
        _jsonParser.execute();
//        UJamGyms gyms = parser.getUJamGyms();
//        return getMockClassList();
    }

    protected void mapGymsToHM(UJamGyms uJamGyms) {

        UJamGym gym;
        int i = 1;
        for (Iterator<UJamGym> iter = uJamGyms.get("gyms").iterator(); iter.hasNext();) {
            gym = iter.next();
            _gymList.put(new Integer(i++), gym);
        }
    }

    public boolean selectedGymHasMultiClasses() {
        return _gymList.get(_selectedGym).getUjamClasses().size() > 1;
    }

    public UJamGym getSelectedGym() {
        return _gymList.get(_selectedGym);
    }

    @Override
    public void onTaskCompleted() {

        Log.d(TAG, "onTaskCompleted called.");
        UJamGyms uJamGyms = _jsonParser.getGyms();
        mapGymsToHM(uJamGyms);
        Log.d(TAG, "Size of gyms=" + uJamGyms.size());

        if (_listener != null) {
            _listener.onTaskCompleted();
        }
    }

    protected LinkedHashMap<Integer, UJamGym> getMockClassList() {

        UJamGym gym1 = new UJamGym();
        gym1.setName("24 Hour Fitness - San Ramon");
        gym1.setAddress("4450 Norris Canyon Road, San Ramon, CA 94583");
        gym1.setTelephone("(925) 244-9855");
        gym1.setLatitude(37.77396);
        gym1.setLongitude(-121.9702431);

        UJamClass ujam1 = new UJamClass();
        ujam1.setInstructor("Chin");
//        ujam1.set_dateTime("Mon 8:15pm");
        ujam1.setDay("Mon");
        ujam1.setTime("8:15pm");
        gym1.getUjamClasses().add(ujam1);
        UJamClass ujam2 = new UJamClass();
        ujam2.setInstructor("Martin");
//        ujam2.set_dateTime("Tues 4:30pm");
        ujam2.setDay("Tues");
        ujam2.setTime("4:30pm");
        gym1.getUjamClasses().add(ujam2);
        UJamClass ujam3 = new UJamClass();
        ujam3.setInstructor("Khazz");
//        ujam3.set_dateTime("Wed 8:15pm");
        ujam3.setDay("Wed");
        ujam3.setTime("8:15pm");
        gym1.getUjamClasses().add(ujam3);
        _gymList.put(new Integer(1), gym1);

//        Address location = coder.getFromLocationName("4450 Norris Canyon Road, San Ramon, CA 94583", 5).get(0);

        UJamGym gym2 = new UJamGym();
        gym2.setName("24 Hour Fitness - Livermore");
        gym2.setAddress("2650 Kittyhawk Road, Livermore, CA 94551");
        gym2.setTelephone("(925) 244-9855");
        gym2.setLatitude(37.6995012);
        gym2.setLongitude(-121.8145431);

        UJamClass ujam4 = new UJamClass();
        ujam4.setInstructor("Jessica");
//        ujam4.set_dateTime("Wed 8:00pm");
        ujam4.setDay("Wed");
        ujam4.setTime("8:00pm");
        gym2.getUjamClasses().add(ujam4);
        _gymList.put(new Integer(2), gym2);

        UJamGym gym3 = new UJamGym();
        gym3.setName("24 Hour Fitness - Pleasanton");
        gym3.setAddress("4770 Willow Road, Pleasanton, CA 94588");
        gym3.setTelephone("(925) 463-1515");
        gym3.setLatitude(37.6979208);
        gym3.setLongitude(-121.8990452);

        UJamClass ujam5 = new UJamClass();
        ujam5.setInstructor("Gina");
//        ujam5.set_dateTime("Tues 7:15pm");
        ujam5.setDay("Tues");
        ujam5.setTime("7:15pm");
        gym3.getUjamClasses().add(ujam5);
        _gymList.put(new Integer(3), gym3);

        return _gymList;
    }
}
