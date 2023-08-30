package com.i2i.img2impclock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import com.flask.colorpicker.ColorPickerView;
import java.time.OffsetTime;

public class Alltask extends AppCompatActivity {
    private ColorPickerView cpv;
    private ImageView cnlpop,setpop;
    private Context mContext;
    private ConstraintLayout constraintLayout;
    private PopupWindow mPopupWindow;
    int[][] smhcolor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alltask);
        mContext = getApplicationContext();
        constraintLayout = findViewById(R.id.mainlayout);
        smhcolor = new int[3][3];

    }


    public void timedata(View view) {
        OffsetTime offset = OffsetTime.now();
        String dataString  = "t|"+offset.getSecond()+","+offset.getMinute()+"!"+(offset.getHour()%12)+".";
        System.out.println("mytime :"+dataString);
        Btfiles.sendString(dataString);
    }

    public void makeColorPopup(final char f)
    {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.colorpicker,null);
        cpv = customView.findViewById(R.id.color_picker_view);
        cnlpop  = customView.findViewById(R.id.btncl);
        setpop  = customView.findViewById(R.id.btnset);
        setpop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int intColor = cpv.getSelectedColor();
                int red = Color.red(intColor);
                int green = Color.green(intColor);
                int blue = Color.blue(intColor);
                String data = (f+"|"+red+","+green+"!"+blue+".");
                Btfiles.sendString(data);
                System.out.println("color : "+data+" alfa : "+Color.alpha(intColor)+" int : "+intColor);
            }
        });
        cnlpop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupWindow.dismiss();
            }
        });
        mPopupWindow = new PopupWindow(
                customView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        mPopupWindow.showAtLocation(constraintLayout,Gravity.CENTER,0,0);
    }

    public void secondColor(View view) {
        makeColorPopup('s');
    }
    public void minitColor(View view) {
        makeColorPopup('m');
    }
    public void hourColor(View view) {
        makeColorPopup('h');
    }
}
