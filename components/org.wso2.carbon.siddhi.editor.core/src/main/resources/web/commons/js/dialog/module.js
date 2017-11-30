/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

define(['./save-to-file-dialog', './replace-confirm-dialog', './delete-confirm-dialog', './open-file-dialog', 
        './close-confirm-dialog', './import-file-dialog', './export-file-dialog', './settings-dialog'],
    function (SaveToFileDialog, ReplaceConfirmDialog, DeleteConfirmDialog, OpenFileDialog, CloseConfirmDialog, 
              ImportFileDialog, ExportFileDialog, SettingsDialog) {
        return {
            save_to_file_dialog: SaveToFileDialog,
            //FolderOpenDialog: FolderOpenDialog,
            //NewItemDialog: NewItemDialog,
            //DeleteItemDialog: DeleteItemDialog,
            open_file_dialog: OpenFileDialog,
            CloseConfirmDialog: CloseConfirmDialog,
            ReplaceConfirmDialog: ReplaceConfirmDialog,
            DeleteConfirmDialog: DeleteConfirmDialog,
            import_file_dialog: ImportFileDialog,
            export_file_dialog: ExportFileDialog,
            settings_dialog: SettingsDialog
        };
    });