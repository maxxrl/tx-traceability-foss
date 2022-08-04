/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { Part } from '@page/parts/model/parts.model';
import { TableEventConfig } from '@shared/components/table/table.model';
import { screen, waitFor } from '@testing-library/angular';
import { server } from '@tests/mock-server';
import { renderComponent } from '@tests/test-render.utils';
import { firstValueFrom } from 'rxjs';
import { OtherPartsModule } from '../other-parts.module';
import { OtherPartsComponent } from './other-parts.component';

describe('Other Parts', () => {
  beforeAll(() => server.listen());
  afterEach(() => server.resetHandlers());
  afterAll(() => server.close());

  const renderOtherParts = () =>
    renderComponent(OtherPartsComponent, {
      imports: [OtherPartsModule],
      translations: ['page.otherParts'],
    });

  it('should render part header', async () => {
    await renderOtherParts();

    expect(screen.getByText('Other Parts')).toBeInTheDocument();
  });

  it('should render part table', async () => {
    await renderOtherParts();

    expect((await screen.findAllByTestId('table-component--test-id')).length).toEqual(1);
  });

  it('should render table and display correct amount of rows', async () => {
    await renderOtherParts();

    const tableElement = await screen.findByTestId('table-component--test-id');
    expect(tableElement).toBeInTheDocument();
    expect(tableElement.children[1].childElementCount).toEqual(4);
  });

  it('should render other parts with closed sidenav', async () => {
    await renderOtherParts();

    const sideNavElements = await screen.findAllByTestId('sidenav--test-id');
    const sideNavElement = sideNavElements[0];

    expect(sideNavElement).toBeInTheDocument();
    expect(sideNavElement).not.toHaveClass('sidenav--container__open');
  });

  it('should render tabs', async () => {
    await renderOtherParts();
    const tabElements = await screen.findAllByRole('tab');

    expect(tabElements.length).toEqual(2);
  });

  it('should render selected parts information', async () => {
    await renderOtherParts();
    await screen.findByTestId('table-component--test-id');
    const selectedPartsInfo = await screen.getByText('0 Parts selected for this page.');

    expect(selectedPartsInfo).toBeInTheDocument();
  });

  it('should set currentSelectedParts correctly', async () => {
    const renderedComponent = await renderOtherParts();
    const expected = ['test', 'test2'];

    renderedComponent.fixture.componentInstance.onMultiSelect(expected);
    expect(renderedComponent.fixture.componentInstance.selectedItems).toEqual([expected]);

    renderedComponent.fixture.componentInstance.onTabChange({ index: 1 } as any);
    renderedComponent.fixture.componentInstance.onMultiSelect(expected);
    expect(renderedComponent.fixture.componentInstance.selectedItems).toEqual([expected, expected]);
  });

  it('should clear currentSelectedParts', async () => {
    const { fixture } = await renderOtherParts();
    const { componentInstance } = fixture;

    const expected = ['test', 'test2'];

    componentInstance.onMultiSelect(expected);
    componentInstance.onTabChange({ index: 1 } as any);
    componentInstance.onMultiSelect(expected);
    expect(componentInstance.selectedItems).toEqual([expected, expected]);

    componentInstance.onTabChange({ index: 0 } as any);
    componentInstance.clearSelected();
    expect(componentInstance.selectedItems).toEqual([[], expected]);
  });

  it('should remove only one item from current selection', async () => {
    const { fixture } = await renderOtherParts();
    const { componentInstance } = fixture;

    const expected = [{ id: 'test' }, { id: 'test2' }];

    componentInstance.onMultiSelect(expected);
    componentInstance.onTabChange({ index: 1 } as any);
    componentInstance.onMultiSelect(expected);
    expect(componentInstance.selectedItems).toEqual([expected, expected]);

    componentInstance.onTabChange({ index: 0 } as any);
    componentInstance.removeItemFromSelection({ id: 'test' } as any);
    expect(componentInstance.selectedItems).toEqual([[{ id: 'test2' }], expected]);
  });

  describe('onTableConfigChange', () => {
    it('should set supplier parts if first tab is selected', async () => {
      const { fixture } = await renderOtherParts();
      const spy = jest.spyOn((fixture.componentInstance as any).otherPartsFacade, 'setSupplierParts');
      const expected: TableEventConfig = { page: 10, pageSize: 10, sorting: ['name', 'asc'] };

      fixture.componentInstance.selectedTab = 0;
      fixture.componentInstance.onTableConfigChange(expected);
      expect(spy).toHaveBeenLastCalledWith(expected.page, expected.pageSize, expected.sorting);
    });

    it('should set customer parts if second tab is selected', async () => {
      const { fixture } = await renderOtherParts();
      const spy = jest.spyOn((fixture.componentInstance as any).otherPartsFacade, 'setCustomerParts');
      const expected: TableEventConfig = { page: 1, pageSize: 20, sorting: ['id', 'asc'] };

      fixture.componentInstance.selectedTab = 1;
      fixture.componentInstance.onTableConfigChange(expected);
      expect(spy).toHaveBeenLastCalledWith(expected.page, expected.pageSize, expected.sorting);
    });
  });

  it('should add item to current list', async () => {
    const { fixture } = await renderOtherParts();
    const expectedPart = { id: 'someId', name: 'some name' } as Part;

    fixture.componentInstance.selectedTab = 0;

    const partPromise = firstValueFrom(fixture.componentInstance.addPartTrigger$);
    fixture.componentInstance.addItemToSelection(expectedPart);

    expect(await partPromise).toEqual(expectedPart);

    expect(fixture.componentInstance.currentSelectedItems).toEqual([expectedPart]);
    fixture.componentInstance.selectedTab = 1;
    expect(fixture.componentInstance.currentSelectedItems).toEqual([]);
  });
});
