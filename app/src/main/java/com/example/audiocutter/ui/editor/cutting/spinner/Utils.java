/*
 * Copyright (C) 2016 Jared Rummler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.audiocutter.ui.editor.cutting.spinner;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

final class Utils {
  static int darker(int color, float factor) {
    return Color.argb(Color.alpha(color), Math.max((int) (Color.red(color) * factor), 0),
        Math.max((int) (Color.green(color) * factor), 0), Math.max((int) (Color.blue(color) * factor), 0));
  }
  static int lighter(int color, float factor) {
    int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
    int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
    int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
    return Color.argb(Color.alpha(color), red, green, blue);
  }
  static boolean isRtl(Context context) {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
        && context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
  }
  static Drawable getDrawable(Context context, int id) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return context.getDrawable(id);
    }
    return context.getResources().getDrawable(id);
  }
}
