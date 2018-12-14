package com.example.android.filmespopulares2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ReviewDialogFragment extends DialogFragment {
    private static final String CONTENT_PARAM = "content";
    private static final String AUTHOR_PARAM = "author";

    // Classe estática necessária para inicializar um DialogFragment passando alguns dados via
    // Bundle para ser usado pelo próprio fragmento.
    public static ReviewDialogFragment newInstance(String content, String author) {
        ReviewDialogFragment f = new ReviewDialogFragment();

        Bundle args = new Bundle();
        args.putString(CONTENT_PARAM, content);
        args.putString(AUTHOR_PARAM, author);

        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootview = inflater.inflate(R.layout.fragment_dialog_review, null);

        TextView contentTextView = (TextView) rootview.findViewById(R.id.tv_content);
        TextView authorTextView = (TextView) rootview.findViewById(R.id.tv_author);

        contentTextView.setText(getArguments().getString(CONTENT_PARAM));
        authorTextView.setText("(" + getArguments().getString(AUTHOR_PARAM) + ")");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootview);
        builder.setNegativeButton(R.string.review_dialog_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}
