/*
 * Copyright (C) 2019 The Android Open Source Project
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
 */
package com.dot.customizations.module;

import com.dot.customizations.model.grid.GridOption;

/**
 * Extension of {@link UserEventLogger} that adds ThemePicker specific events.
 */
public interface ThemesUserEventLogger extends UserEventLogger {

    /**
     * Logs the color usage while color is applied.
     *
     * @param action     color applied action.
     * @param colorIndex color applied index.
     */
    void logColorApplied(int action, int colorIndex);

    void logGridSelected(GridOption grid);

    void logGridApplied(GridOption grid);

}
