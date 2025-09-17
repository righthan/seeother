package com.seeother.ui.recommend;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.seeother.R;
import com.seeother.manager.RecommendLinkManager;

import java.util.List;

public class RecommendLinksFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private Slider probabilitySlider;
    private android.widget.TextView tvProbabilityValue;
    private View emptyView;
    private RecommendLinkManager linkManager;
    private RecommendLinksAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        linkManager = RecommendLinkManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend_links, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadData();
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.rv_links);
        fabAdd = view.findViewById(R.id.fab_add_link);
        probabilitySlider = view.findViewById(R.id.slider_probability);
        tvProbabilityValue = view.findViewById(R.id.tv_probability_value);
        emptyView = view.findViewById(R.id.empty_view);
        
        // 设置概率滑块的初始值
        probabilitySlider.setValue(linkManager.getProbability());
        updateProbabilityText((int) probabilitySlider.getValue());
    }

    private void setupRecyclerView() {
        adapter = new RecommendLinksAdapter(requireContext(), new RecommendLinksAdapter.OnLinkActionListener() {
            @Override
            public void onEditLink(String link) {
                showEditLinkDialog(link);
            }

            @Override
            public void onDeleteLink(String link) {
                showDeleteConfirmDialog(link);
            }
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> showAddLinkDialog());
        
        probabilitySlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                linkManager.setProbability((int) value);
                updateProbabilityText((int) value);
            }
        });
    }

    private void loadData() {
        List<String> links = linkManager.getAllLinks();
        adapter.setLinks(links);
        
        // 更新空状态显示
        updateEmptyState(links.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showAddLinkDialog() {
        showLinkInputDialog("添加推荐链接", "", (link) -> {
            if (linkManager.isValidLink(link)) {
                if (!linkManager.linkExists(link)) {
                    linkManager.addLink(link);
                    loadData();
                    Toast.makeText(requireContext(), "链接添加成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "链接已存在", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "请输入有效的链接格式", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditLinkDialog(String originalLink) {
        showLinkInputDialog("编辑推荐链接", originalLink, (newLink) -> {
            if (linkManager.isValidLink(newLink)) {
                if (!linkManager.linkExists(newLink) || newLink.equals(originalLink)) {
                    linkManager.updateLink(originalLink, newLink);
                    loadData();
                    Toast.makeText(requireContext(), "链接更新成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "链接已存在", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "请输入有效的链接格式", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmDialog(String link) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除链接")
                .setMessage("确定要删除这个链接吗？\n\n" + link)
                .setPositiveButton("删除", (dialog, which) -> {
                    linkManager.removeLink(link);
                    loadData();
                    Toast.makeText(requireContext(), "链接删除成功", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showLinkInputDialog(String title, String initialText, OnLinkInputListener listener) {
        Context context = requireContext();
        
        // 创建输入框
        EditText editText = new EditText(context);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        editText.setHint("请输入链接 (如: https://... 或 bilibili://...)");
        editText.setText(initialText);
        editText.setSelection(editText.getText().length());
        
        // 设置布局参数
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(50, 20, 50, 20);
        editText.setLayoutParams(layoutParams);
        
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(editText)
                .setPositiveButton("确定", (dialog, which) -> {
                    String input = editText.getText().toString().trim();
                    if (!input.isEmpty()) {
                        listener.onLinkInput(input);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateProbabilityText(int probability) {
        if (tvProbabilityValue != null) {
            tvProbabilityValue.setText("当前概率: " + probability + "%");
        }
    }

    private interface OnLinkInputListener {
        void onLinkInput(String link);
    }
} 