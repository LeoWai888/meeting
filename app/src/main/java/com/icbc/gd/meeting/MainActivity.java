package com.icbc.gd.meeting;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.animation.BaseAnimation;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Model> datas;

    private MyAdapter adapter;

    private RecyclerView recyclerView;

    private WebSocketClient mSocketClient;

    private TextView mNumber;

    private RelativeLayout mAddmore;

    private RelativeLayout mAddimg;

    private ImageView mImg;

//    private Button testBt;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Model model;
            super.handleMessage(msg);
            SignJson signJson= JSON.parseObject(msg.obj.toString(),SignJson.class);
            if(signJson!=null) {
                model = new Model();
                model.setTitle(signJson.getName().toString()+"   "+signJson.getUserDept());
                model.setContent(signJson.getCompareTime().toString());
                model.setImgBase64(signJson.getCardImg());
                datas.add(0,model);   //数据添加到0位置
//                changeImg(signJson.getCardImg());  //显示照片

//                Bitmap bitmap;
//                byte[] bitmapArray;
//                bitmapArray = Base64.decode(signJson.getCardImg(), Base64.DEFAULT);
//                bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
//                mImg.setImageBitmap(bitmap);

                number();    //数字变化
//                ObjectAnimator animator=ObjectAnimator.ofFloat(mAddimg,"alpha",7f,0f);
//                mAddimg.setVisibility(View.VISIBLE);   //显示+1图片
//                animator.setDuration(500);
//                animator.start();
                adapter.notifyDataSetChanged();   //更新listview并显示
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        datas = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mNumber=(TextView)findViewById(R.id.number_tv);
        mAddmore=(RelativeLayout)findViewById(R.id.addmore_rl);
//        testBt=(Button)findViewById(R.id.test_bt);
        mImg=(ImageView)findViewById(R.id.iv_img);
        mAddimg=(RelativeLayout)findViewById(R.id.addimg_rl);

        datas = new ArrayList<>();
//        testBt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                add();
//                number();
//                ObjectAnimator animator=ObjectAnimator.ofFloat(mAddimg,"alpha",7f,0f);
//                mAddimg.setVisibility(View.VISIBLE);
//                animator.setDuration(500);
//                animator.start();
//            }
//        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager); //创建适配器
        adapter = new MyAdapter(R.layout.item_rv, datas); //
        adapter.openLoadAnimation(new BaseAnimation() {
            @Override
            public Animator[] getAnimators(View view) {
                return new Animator[]{
                        ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1),
                        ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1),
                };
            }

        });
        getPeople();

    }

    private void add() {
        Model model = new Model();
        model.setTitle("工小智 小象俱乐部");
        model.setContent("2018-10-23  9:30");
        datas.add(0,model);
        adapter.notifyDataSetChanged();
    }

    public void number()
    {
        String num;
        ObjectAnimator animator=ObjectAnimator.ofFloat(mAddmore,"alpha",0f,7f,0f);
        animator.setDuration(10000);
        animator.start();
        recyclerView.setAdapter(adapter);
        num=String.valueOf(datas.size());
        mNumber.setText(num);
    }

    public void changeImg(String string)
    {
        Bitmap bitmap;
        byte[] bitmapArray;
        bitmapArray = Base64.decode(string, Base64.DEFAULT);
        bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        mImg.setImageBitmap(bitmap);
    }

    //websocket
    private void getPeople()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //TODO 切换URL为自己的IP
                    mSocketClient = new WebSocketClient(new URI(getString(R.string.URI_name)), new Draft_17()) {
                        @Override
                        public void onOpen(ServerHandshake handshakedata) {
                            Log.d("picher_log", "run() return:" + "连接到服务器");
                        }

                        @Override
                        public void onMessage(String message) {
                            Log.d("picher_log", "接收消息" + message);
//                            SignJson signJson= JSON.parseObject(message.toString(),SignJson.class);
                            handler.obtainMessage(0, message).sendToTarget();
                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote) {
                            Log.d("picher_log", "通道关闭");
                        }

                        @Override
                        public void onError(Exception ex) {
                            Log.d("picher_log", "链接错误");
                        }
                    };
                    mSocketClient.connect();

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocketClient != null) {
            mSocketClient.close();
        }
    }


    class Model {
        public String title;
        public String content;
//        public String imgBase64;
        public Bitmap imgBit;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Bitmap getImg() {
            return imgBit;
        }

        public void setImgBase64(String imgBase64) {
//            this.imgBase64 = imgBase64;
            Bitmap bitmap;
            byte[] bitmapArray;
            bitmapArray = Base64.decode(imgBase64, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
//            mImg.setImageBitmap(bitmap);
            this.imgBit=bitmap;

        }
    }

    class MyAdapter extends BaseQuickAdapter<Model, BaseViewHolder> {
        public MyAdapter(@LayoutRes int layoutResId, @Nullable List<Model> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, Model item) {
            //可链式调用赋值
            helper.setText(R.id.tv_title, item.getTitle()).
                    setText(R.id.tv_time, item.getContent()).
                    setImageBitmap(R.id.iv_img, item.getImg());
        }
    }

}

