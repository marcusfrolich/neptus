/*
 * Copyright (c) 2004-2013 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENSE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://www.lsts.pt/neptus/licence.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: pdias
 * Aug 11, 2013
 */
package pt.up.fe.dceg.neptus.gui.editor.renderer;

import java.awt.Component;

import javax.swing.JTable;

import pt.up.fe.dceg.neptus.i18n.I18n;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

/**
 * @author pdias
 *
 */
@SuppressWarnings("serial")
public class I18nCellRenderer extends DefaultCellRenderer {

    {
        setShowOddAndEvenRows(false);
    }

    /* (non-Javadoc)
     * @see com.l2fprod.common.swing.renderer.DefaultCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        String stringToI18n = null;
        if (value instanceof String)
            stringToI18n = (String) value;
        else
            stringToI18n = value.toString();
        return super.getTableCellRendererComponent(table, I18n.text(stringToI18n), isSelected, hasFocus, row, column);
    }
}