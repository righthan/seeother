package com.seeother.ui.recommend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.seeother.R;

import java.util.ArrayList;
import java.util.List;

public class RecommendLinksAdapter extends RecyclerView.Adapter<RecommendLinksAdapter.LinkViewHolder> {

    private final Context context;
    private List<String> links;
    private final OnLinkActionListener listener;

    public interface OnLinkActionListener {
        void onEditLink(String link);
        void onDeleteLink(String link);
    }

    public RecommendLinksAdapter(Context context, OnLinkActionListener listener) {
        this.context = context;
        this.links = new ArrayList<>();
        this.listener = listener;
    }

    public void setLinks(List<String> links) {
        this.links = links != null ? new ArrayList<>(links) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommend_link, parent, false);
        return new LinkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LinkViewHolder holder, int position) {
        String link = links.get(position);
        holder.bind(link);
    }

    @Override
    public int getItemCount() {
        return links.size();
    }

    class LinkViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLink;
        private final TextView tvLinkType;

        public LinkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLink = itemView.findViewById(R.id.tv_link);
            tvLinkType = itemView.findViewById(R.id.tv_link_type);
            ImageButton btnEdit = itemView.findViewById(R.id.btn_edit);
            ImageButton btnDelete = itemView.findViewById(R.id.btn_delete);

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditLink(links.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteLink(links.get(position));
                }
            });
        }

        public void bind(String link) {
            tvLink.setText(link);
            
            // 根据链接类型设置标签
            String linkType = getLinkType(link);
            tvLinkType.setText(linkType);
            tvLinkType.setBackgroundResource(getLinkTypeBackground(link));
        }

        private String getLinkType(String link) {
            if (link.startsWith("https://") || link.startsWith("http://")) {
                return "网页";
            } else if (link.startsWith("bilibili://")) {
                return "哔哩哔哩";
            } else if (link.contains("://")) {
                return "应用";
            } else {
                return "未知";
            }
        }

        private int getLinkTypeBackground(String link) {
            if (link.startsWith("https://") || link.startsWith("http://")) {
                return R.drawable.link_type_web_bg;
            } else if (link.startsWith("bilibili://")) {
                return R.drawable.link_type_bilibili_bg;
            } else {
                return R.drawable.link_type_app_bg;
            }
        }
    }
} 